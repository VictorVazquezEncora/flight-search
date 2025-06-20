export default {
  testEnvironment: "jsdom",
  transform: {
    "^.+\\.(ts|tsx|js|jsx)$": "babel-jest",
  },
  moduleNameMapper: {
    "^@/(.*)$": "<rootDir>/src/$1",
    "^../../../lib/fetch$": "<rootDir>/src/lib/__mocks__/fetch.ts",
  },
  setupFilesAfterEnv: ["<rootDir>/src/setupTests.ts"],
  moduleDirectories: ["node_modules", "src"],
  testEnvironmentOptions: {
    customExportConditions: [""],
  },
};
