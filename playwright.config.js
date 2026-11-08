import {defineConfig} from '@playwright/test';
export default defineConfig({testDir:'./tests',testMatch:'browser.spec.js',timeout:45000,workers:2,use:{baseURL:'http://127.0.0.1:4173',browserName:'chromium',screenshot:'only-on-failure'},webServer:{command:'node scripts/serve.mjs dist',url:'http://127.0.0.1:4173',reuseExistingServer:!process.env.CI},reporter:'list'});
