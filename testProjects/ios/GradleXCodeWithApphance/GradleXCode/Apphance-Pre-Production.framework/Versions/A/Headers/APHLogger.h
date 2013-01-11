//
//  Apphance-iOS.h
//  Apphance-iOS
//
//  Created by Piotr Wach, Paul Dudek on 10-01-25.
//  Copyright 2010 Apphance. All rights reserved.
//
//	This is the main logger file. Include it in your project
//  along with the Apphance Framework. 

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "APHDefines.h"

/* 
 * Enum preseting possible logging level
 */
typedef enum {
	APHLogLevelFatal = 16,
	APHLogLevelError = 8,
	APHLogLevelWarning = 4,
	APHLogLevelInfo = 2,
	APHLogLevelVerbose = 0
} APHLogLevel;

/*
 * Convinience method to log applications exceptions
 */
APH_EXTERN void APHUncaughtExceptionHandler(NSException* exception);

/** Logging macros **/

/*
 * Replace all occurences of NSLog with APHLog. 
 * Except for working like normal log it will also send message to Apphance server.
 */
#define APHLog(nsstring_format, ...)	\
	do {						\
		[APHLogger logWithLevel:APHLogLevelInfo \
		tag:nil \
		line:__LINE__ fileName:[NSString stringWithUTF8String:__FILE__] \
		method:[NSString stringWithUTF8String:__PRETTY_FUNCTION__] \
		stacktrace:[NSThread callStackReturnAddresses]\
		format:nsstring_format, \
		##__VA_ARGS__];\
} while(0)

/*
 * Works as the one above, except it provides additional configuration options
 */
#define APHExtendedLog(APHLogLevel, nsstring_tag, nsstring_format, ...)	\
	do {						\
		[APHLogger logWithLevel:(APHLogLevel) \
		tag:(nsstring_tag) \
		line:__LINE__ fileName:[NSString stringWithUTF8String:__FILE__] \
		method:[NSString stringWithUTF8String:__PRETTY_FUNCTION__] \
		stacktrace:[NSThread callStackReturnAddresses] \
		format:nsstring_format, \
		##__VA_ARGS__];\
} while(0)

@interface APHLogger : NSObject {
	
}

/** General setup methods **/

/*
 * This will override current application version name. 
 * Default one is obtained by CFBundleShortVersionString. 
 * Be sure to call this before starting Apphance session otherwise it will have no effect. 
 */
+ (void) setVersionName:(NSString*) versioName;

/*
 * This will override current application version code. 
 * Default one is obtained by CFBundleVersion. 
 * Be sure to call this before starting Apphance session otherwise it will have no effect. 
 */
+ (void) setVersionNumber:(NSString*) setVersionNumber;

/*
 * Logs exception. Screenshot will be included in the data sent to 
 * the server. You can use APHUncaughtExceptionHandler as a convinience accessor to this method.
 */
+ (void) logApplicationException:(NSException *) error;

/*
 * This method will register object for logging, meaning each and
 * every method sent to it will be logged, including timestamp.
 * See documentation for more information.
 */
+ (id) registerObjectForLogging:(id) object;

/*
 * Forces the contents of the session log buffer to be send. 
 */
+ (void) flush;

/** Starting new session **/

/*
 * Starts APH session. Should be called once per application run - doing otherwise will result in an undefined behavior.
 * You should pass a valid apphance mode.
 */
+ (void) startNewSessionWithApplicationKey:(NSString *)applicationID apphanceMode:(NSString*) apphanceMode;

+ (void) startNewSessionWithApplicationKey:(NSString *)applicationID apphanceMode:(NSString*) apphanceMode withUtest:(BOOL) withUtest;

/** Market mode specific **/

/*
 * Calling this method will force apphance to upload session data even if there was no crash in it.
 * Works only in Market mode. 
 */
- (void) forceSessionUploadOnNextStartup;

@end

/*
 * Despite being called private, you can generaly call these method, if you wish. However, this is strongly discouraged, since given macros and functions
 * are more conviniet way of using APH. 
 */
@interface APHLogger (PrivateAccessors) 

+ (void) logWithLevel:(APHLogLevel) level tag:(NSString *) tag line:(NSInteger) line fileName:(NSString *)fileName method:(NSString *) method stacktrace:(NSArray*)stacktrace format:(NSString *)format, ...;

@end
