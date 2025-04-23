module.exports = {
    env: {
        es6: true,
        node: true,
    },
    parserOptions: {
        ecmaVersion: 2018,
    },
    extends: [
        'eslint:recommended',
    ],
    rules: {
        'quotes': ['error', 'single'],
        'indent': ['error', 4],
        'max-len': ['error', { 'code': 120 }],
        'object-curly-spacing': ['error', 'always'],
        'comma-dangle': ['error', 'always-multiline'],
    },
};
