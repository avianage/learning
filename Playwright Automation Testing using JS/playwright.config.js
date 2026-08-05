// @ts-check
import { defineConfig, devices } from '@playwright/test';

/**
 * Read environment variables from file.
 * https://github.com/motdotla/dotenv
 */
// import dotenv from 'dotenv';
// import path from 'path';
// dotenv.config({ path: path.resolve(__dirname, '.env') });

/**
 * @see https://playwright.dev/docs/test-configuration
 */
export default defineConfig({
	testDir: './tests',
	// Default timeout is 30 seconds
	timeout: 40 * 1000,
	// For assertion validations
	expect: {
		timeout: 5 * 1000
	},
	reporter: 'html',
	use: {
		browserName: 'chromium',
		headless: false
	}
});

