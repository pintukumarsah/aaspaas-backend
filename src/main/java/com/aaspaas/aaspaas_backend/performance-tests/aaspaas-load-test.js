import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

// ========================================================
// AASPAAS PART 38 - LOAD & PERFORMANCE TESTING
// Default backend: http://localhost:9090
//
// Safe by default: only GET requests are sent.
// No orders, payments, refunds or payouts are created.
// ========================================================

const BASE_URL = (
    __ENV.BASE_URL || 'http://localhost:9090'
).replace(/\/+$/, '');

const CATEGORY_PATH =
    __ENV.CATEGORY_PATH || '/api/categories';

const PRODUCT_PATH =
    __ENV.PRODUCT_PATH || '/api/products';

const ORDERS_PATH =
    __ENV.ORDERS_PATH || '/api/orders';

const ACCESS_TOKEN = __ENV.ACCESS_TOKEN || '';

const PROFILE = (__ENV.PROFILE || 'load').toLowerCase();

const failures = new Rate('aaspaas_failed_checks');
const apiErrors = new Counter('aaspaas_api_errors');
const categoryDuration = new Trend(
    'aaspaas_category_response_ms',
    true
);
const productDuration = new Trend(
    'aaspaas_product_response_ms',
    true
);
const orderDuration = new Trend(
    'aaspaas_order_response_ms',
    true
);

// Profile definitions:
// smoke: 1 virtual user, short validation
// load: normal concurrent load
// stress: gradually increase concurrency
// spike: sudden traffic increase
const profileStages = {
    smoke: [
        { duration: '10s', target: 1 }
    ],

    load: [
        { duration: '30s', target: 5 },
        { duration: '1m', target: 10 },
        { duration: '2m', target: 20 },
        { duration: '30s', target: 0 }
    ],

    stress: [
        { duration: '30s', target: 10 },
        { duration: '1m', target: 25 },
        { duration: '1m', target: 50 },
        { duration: '1m', target: 75 },
        { duration: '30s', target: 0 }
    ],

    spike: [
        { duration: '20s', target: 5 },
        { duration: '10s', target: 50 },
        { duration: '30s', target: 50 },
        { duration: '10s', target: 0 }
    ]
};

if (!profileStages[PROFILE]) {
    throw new Error(
        `Invalid PROFILE "${PROFILE}". ` +
        'Use smoke, load, stress or spike.'
    );
}

export const options = {
    stages: profileStages[PROFILE],

    thresholds: {
        http_req_failed: ['rate<0.01'],
        aaspaas_failed_checks: ['rate<0.01'],
        http_req_duration: ['p(95)<800', 'p(99)<1500']
    },

    discardResponseBodies: true,

    tags: {
        application: 'aaspaas-backend',
        test_profile: PROFILE
    }
};

function buildHeaders() {
    const headers = {
        Accept: 'application/json'
    };

    if (ACCESS_TOKEN) {
        headers.Authorization = `Bearer ${ACCESS_TOKEN}`;
    }

    return headers;
}

function recordResponse(response, metric, label) {
    metric.add(response.timings.duration);

    const passed = check(response, {
        [`${label}: status is below 400`]:
            (r) => r.status >= 200 && r.status < 400,

        [`${label}: response under 2 seconds`]:
            (r) => r.timings.duration < 2000
    });

    failures.add(!passed);

    if (!passed) {
        apiErrors.add(1);
    }
}

function getPublicApi(path, metric, label) {
    const response = http.get(`${BASE_URL}${path}`, {
        headers: buildHeaders(),
        tags: { endpoint: label },
        timeout: '10s'
    });

    recordResponse(response, metric, label);
}

function getProtectedApi(path, metric, label) {
    // Do not send unauthenticated requests to protected APIs
    // during this load profile.
    if (!ACCESS_TOKEN) {
        return;
    }

    const response = http.get(`${BASE_URL}${path}`, {
        headers: buildHeaders(),
        tags: { endpoint: label },
        timeout: '10s'
    });

    recordResponse(response, metric, label);
}

export default function () {
    // Public read-only endpoint tests.
    getPublicApi(
        CATEGORY_PATH,
        categoryDuration,
        'categories'
    );

    sleep(0.2);

    getPublicApi(
        PRODUCT_PATH,
        productDuration,
        'products'
    );

    // Optional authenticated read-only endpoint test.
    // Supply a valid access token to enable it.
    getProtectedApi(
        ORDERS_PATH,
        orderDuration,
        'orders'
    );

    sleep(0.5);
}

export function handleSummary(data) {
    const metrics = data.metrics || {};

    function metricValue(name, key) {
        const metric = metrics[name];

        if (!metric || !metric.values) {
            return 'N/A';
        }

        const value = metric.values[key];

        return value === undefined
            ? 'N/A'
            : Number(value).toFixed(2);
    }

    const summary = [
        'AASPAAS PART 38 - PERFORMANCE SUMMARY',
        '======================================',
        `Profile: ${PROFILE}`,
        `Base URL: ${BASE_URL}`,
        '',
        `Total HTTP requests: ${
            metricValue('http_reqs', 'count')
        }`,
        `Failed HTTP request rate: ${
            metricValue('http_req_failed', 'rate')
        }`,
        `Average response time (ms): ${
            metricValue('http_req_duration', 'avg')
        }`,
        `P95 response time (ms): ${
            metricValue('http_req_duration', 'p(95)')
        }`,
        `P99 response time (ms): ${
            metricValue('http_req_duration', 'p(99)')
        }`,
        '',
        `Category average (ms): ${
            metricValue('aaspaas_category_response_ms', 'avg')
        }`,
        `Product average (ms): ${
            metricValue('aaspaas_product_response_ms', 'avg')
        }`,
        `Order average (ms): ${
            metricValue('aaspaas_order_response_ms', 'avg')
        }`,
        '',
        'Note: thresholds are targets, not guaranteed results.'
    ].join('\n');

    return {
        stdout: `${summary}\n`,
        'aaspaas-part38-summary.json':
            JSON.stringify(data, null, 2)
    };
}