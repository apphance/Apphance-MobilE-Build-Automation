set -e
set -x
if [ "$RUN_UNIT_TEST_WITH_IOS_SIM" = "YES" ]; then
    test_bundle_path="$BUILT_PRODUCTS_DIR/$PRODUCT_NAME.$WRAPPER_EXTENSION"
    dir_name_test_host=$(dirname "$TEST_HOST")
    if [ -z "$UNIT_TEST_OUTPUT_FILE" ]; then
        ios-sim launch "$dir_name_test_host" --setenv DYLD_INSERT_LIBRARIES=/../../Library/PrivateFrameworks/IDEBundleInjection.framework/IDEBundleInjection --setenv XCInjectBundle="$test_bundle_path" --setenv XCInjectBundleInto="$TEST_HOST" --args -SenTest All "$test_bundle_path" --exit
    else
        ios-sim launch "$dir_name_test_host" --setenv DYLD_INSERT_LIBRARIES=/../../Library/PrivateFrameworks/IDEBundleInjection.framework/IDEBundleInjection --setenv XCInjectBundle="$test_bundle_path" --setenv XCInjectBundleInto="$TEST_HOST" --stdout "$UNIT_TEST_OUTPUT_FILE" --stderr "$UNIT_TEST_OUTPUT_FILE" --args -SenTest All "$test_bundle_path" --exit
    fi
else
    "${SYSTEM_DEVELOPER_DIR}/Tools/RunUnitTests"
fi
