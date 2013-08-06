if [ "$RUN_UNIT_TEST_WITH_IOS_SIM" = "YES" ]; then
    test_bundle_path="$BUILT_PRODUCTS_DIR/$PRODUCT_NAME.$WRAPPER_EXTENSION"
    environment_args="--setenv DYLD_INSERT_LIBRARIES=/../../Library/PrivateFrameworks/IDEBundleInjection.framework/IDEBundleInjection --setenv XCInjectBundle=$test_bundle_path --setenv XCInjectBundleInto=$TEST_HOST"
    if [ -z "$UNIT_TEST_OUTPUT_FILE" ]; then
        ios-sim launch $(dirname $TEST_HOST) $environment_args --args -SenTest All $test_bundle_path --exit
    else
        ios-sim launch $(dirname $TEST_HOST) $environment_args --stdout $UNIT_TEST_OUTPUT_FILE --stderr $UNIT_TEST_OUTPUT_FILE --args -SenTest All $test_bundle_path --exit
    fi
else
    "${SYSTEM_DEVELOPER_DIR}/Tools/RunUnitTests"
fi

exit 0