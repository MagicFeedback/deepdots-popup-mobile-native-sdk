#import <Foundation/NSArray.h>
#import <Foundation/NSDictionary.h>
#import <Foundation/NSError.h>
#import <Foundation/NSObject.h>
#import <Foundation/NSSet.h>
#import <Foundation/NSString.h>
#import <Foundation/NSValue.h>

@class ComposeAppAction, ComposeAppActionAccept, ComposeAppActionBack, ComposeAppActionComplete, ComposeAppActionDecline, ComposeAppActionStart, ComposeAppActions, ComposeAppCooldownCondition, ComposeAppDeepdots, ComposeAppDeepdotsPopups, ComposeAppEnvironment, ComposeAppEvent, ComposeAppEventData, ComposeAppEvents, ComposeAppHtmlParagraph, ComposeAppHtmlRun, ComposeAppImageAlignment, ComposeAppImageSize, ComposeAppInitOptions, ComposeAppKotlinAbstractCoroutineContextElement, ComposeAppKotlinAbstractCoroutineContextKey<B, E>, ComposeAppKotlinArray<T>, ComposeAppKotlinByteArray, ComposeAppKotlinByteIterator, ComposeAppKotlinCancellationException, ComposeAppKotlinEnum<E>, ComposeAppKotlinEnumCompanion, ComposeAppKotlinException, ComposeAppKotlinIllegalStateException, ComposeAppKotlinKTypeProjection, ComposeAppKotlinKTypeProjectionCompanion, ComposeAppKotlinKVariance, ComposeAppKotlinRuntimeException, ComposeAppKotlinThrowable, ComposeAppKotlinUnit, ComposeAppKotlinx_coroutines_coreCoroutineDispatcher, ComposeAppKotlinx_coroutines_coreCoroutineDispatcherKey, ComposeAppKtor_client_coreHttpClient, ComposeAppKtor_client_coreHttpClientCall, ComposeAppKtor_client_coreHttpClientCallCompanion, ComposeAppKtor_client_coreHttpClientConfig<T>, ComposeAppKtor_client_coreHttpClientEngineConfig, ComposeAppKtor_client_coreHttpReceivePipeline, ComposeAppKtor_client_coreHttpReceivePipelinePhases, ComposeAppKtor_client_coreHttpRequestBuilder, ComposeAppKtor_client_coreHttpRequestBuilderCompanion, ComposeAppKtor_client_coreHttpRequestData, ComposeAppKtor_client_coreHttpRequestPipeline, ComposeAppKtor_client_coreHttpRequestPipelinePhases, ComposeAppKtor_client_coreHttpResponse, ComposeAppKtor_client_coreHttpResponseContainer, ComposeAppKtor_client_coreHttpResponseData, ComposeAppKtor_client_coreHttpResponsePipeline, ComposeAppKtor_client_coreHttpResponsePipelinePhases, ComposeAppKtor_client_coreHttpSendPipeline, ComposeAppKtor_client_coreHttpSendPipelinePhases, ComposeAppKtor_client_coreProxyConfig, ComposeAppKtor_eventsEventDefinition<T>, ComposeAppKtor_eventsEvents, ComposeAppKtor_httpContentType, ComposeAppKtor_httpContentTypeCompanion, ComposeAppKtor_httpHeaderValueParam, ComposeAppKtor_httpHeaderValueWithParameters, ComposeAppKtor_httpHeaderValueWithParametersCompanion, ComposeAppKtor_httpHeadersBuilder, ComposeAppKtor_httpHttpMethod, ComposeAppKtor_httpHttpMethodCompanion, ComposeAppKtor_httpHttpProtocolVersion, ComposeAppKtor_httpHttpProtocolVersionCompanion, ComposeAppKtor_httpHttpStatusCode, ComposeAppKtor_httpHttpStatusCodeCompanion, ComposeAppKtor_httpOutgoingContent, ComposeAppKtor_httpURLBuilder, ComposeAppKtor_httpURLBuilderCompanion, ComposeAppKtor_httpURLProtocol, ComposeAppKtor_httpURLProtocolCompanion, ComposeAppKtor_httpUrl, ComposeAppKtor_httpUrlCompanion, ComposeAppKtor_ioBuffer, ComposeAppKtor_ioBufferCompanion, ComposeAppKtor_ioByteReadPacket, ComposeAppKtor_ioByteReadPacketCompanion, ComposeAppKtor_ioChunkBuffer, ComposeAppKtor_ioChunkBufferCompanion, ComposeAppKtor_ioInput, ComposeAppKtor_ioInputCompanion, ComposeAppKtor_ioMemory, ComposeAppKtor_ioMemoryCompanion, ComposeAppKtor_utilsAttributeKey<T>, ComposeAppKtor_utilsGMTDate, ComposeAppKtor_utilsGMTDateCompanion, ComposeAppKtor_utilsMonth, ComposeAppKtor_utilsMonthCompanion, ComposeAppKtor_utilsPipeline<TSubject, TContext>, ComposeAppKtor_utilsPipelinePhase, ComposeAppKtor_utilsStringValuesBuilderImpl, ComposeAppKtor_utilsTypeInfo, ComposeAppKtor_utilsWeekDay, ComposeAppKtor_utilsWeekDayCompanion, ComposeAppLegacyCondition, ComposeAppMode, ComposeAppPlatformContext, ComposeAppPopupDefinition, ComposeAppPopupOptions, ComposeAppPopupRenderer, ComposeAppPosition, ComposeAppSdkRuntime, ComposeAppSegments, ComposeAppShowOptions, ComposeAppStyle, ComposeAppSurveyProgressState, ComposeAppTheme, ComposeAppTrigger, ComposeAppTriggerClick, ComposeAppTriggerConditionStatus, ComposeAppTriggerEvent, ComposeAppTriggerExit, ComposeAppTriggerScroll, ComposeAppTriggerTimeOnPage, ComposeAppTriggerType, UIViewController;

@protocol ComposeAppKeyValueStorage, ComposeAppKotlinAppendable, ComposeAppKotlinComparable, ComposeAppKotlinContinuation, ComposeAppKotlinContinuationInterceptor, ComposeAppKotlinCoroutineContext, ComposeAppKotlinCoroutineContextElement, ComposeAppKotlinCoroutineContextKey, ComposeAppKotlinFunction, ComposeAppKotlinIterator, ComposeAppKotlinKAnnotatedElement, ComposeAppKotlinKClass, ComposeAppKotlinKClassifier, ComposeAppKotlinKDeclarationContainer, ComposeAppKotlinKType, ComposeAppKotlinMapEntry, ComposeAppKotlinSequence, ComposeAppKotlinSuspendFunction1, ComposeAppKotlinSuspendFunction2, ComposeAppKotlinx_coroutines_coreChildHandle, ComposeAppKotlinx_coroutines_coreChildJob, ComposeAppKotlinx_coroutines_coreCoroutineScope, ComposeAppKotlinx_coroutines_coreDisposableHandle, ComposeAppKotlinx_coroutines_coreJob, ComposeAppKotlinx_coroutines_coreParentJob, ComposeAppKotlinx_coroutines_coreRunnable, ComposeAppKotlinx_coroutines_coreSelectClause, ComposeAppKotlinx_coroutines_coreSelectClause0, ComposeAppKotlinx_coroutines_coreSelectInstance, ComposeAppKtor_client_coreHttpClientEngine, ComposeAppKtor_client_coreHttpClientEngineCapability, ComposeAppKtor_client_coreHttpClientPlugin, ComposeAppKtor_client_coreHttpRequest, ComposeAppKtor_httpHeaders, ComposeAppKtor_httpHttpMessage, ComposeAppKtor_httpHttpMessageBuilder, ComposeAppKtor_httpParameters, ComposeAppKtor_httpParametersBuilder, ComposeAppKtor_ioByteReadChannel, ComposeAppKtor_ioCloseable, ComposeAppKtor_ioObjectPool, ComposeAppKtor_ioReadSession, ComposeAppKtor_utilsAttributes, ComposeAppKtor_utilsStringValues, ComposeAppKtor_utilsStringValuesBuilder, ComposeAppPlatform, ComposeAppPopupsService;

NS_ASSUME_NONNULL_BEGIN
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunknown-warning-option"
#pragma clang diagnostic ignored "-Wincompatible-property-type"
#pragma clang diagnostic ignored "-Wnullability"

#pragma push_macro("_Nullable_result")
#if !__has_feature(nullability_nullable_result)
#undef _Nullable_result
#define _Nullable_result _Nullable
#endif

__attribute__((swift_name("KotlinBase")))
@interface ComposeAppBase : NSObject
- (instancetype)init __attribute__((unavailable));
+ (instancetype)new __attribute__((unavailable));
+ (void)initialize __attribute__((objc_requires_super));
@end

@interface ComposeAppBase (ComposeAppBaseCopying) <NSCopying>
@end

__attribute__((swift_name("KotlinMutableSet")))
@interface ComposeAppMutableSet<ObjectType> : NSMutableSet<ObjectType>
@end

__attribute__((swift_name("KotlinMutableDictionary")))
@interface ComposeAppMutableDictionary<KeyType, ObjectType> : NSMutableDictionary<KeyType, ObjectType>
@end

@interface NSError (NSErrorComposeAppKotlinException)
@property (readonly) id _Nullable kotlinException;
@end

__attribute__((swift_name("KotlinNumber")))
@interface ComposeAppNumber : NSNumber
- (instancetype)initWithChar:(char)value __attribute__((unavailable));
- (instancetype)initWithUnsignedChar:(unsigned char)value __attribute__((unavailable));
- (instancetype)initWithShort:(short)value __attribute__((unavailable));
- (instancetype)initWithUnsignedShort:(unsigned short)value __attribute__((unavailable));
- (instancetype)initWithInt:(int)value __attribute__((unavailable));
- (instancetype)initWithUnsignedInt:(unsigned int)value __attribute__((unavailable));
- (instancetype)initWithLong:(long)value __attribute__((unavailable));
- (instancetype)initWithUnsignedLong:(unsigned long)value __attribute__((unavailable));
- (instancetype)initWithLongLong:(long long)value __attribute__((unavailable));
- (instancetype)initWithUnsignedLongLong:(unsigned long long)value __attribute__((unavailable));
- (instancetype)initWithFloat:(float)value __attribute__((unavailable));
- (instancetype)initWithDouble:(double)value __attribute__((unavailable));
- (instancetype)initWithBool:(BOOL)value __attribute__((unavailable));
- (instancetype)initWithInteger:(NSInteger)value __attribute__((unavailable));
- (instancetype)initWithUnsignedInteger:(NSUInteger)value __attribute__((unavailable));
+ (instancetype)numberWithChar:(char)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedChar:(unsigned char)value __attribute__((unavailable));
+ (instancetype)numberWithShort:(short)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedShort:(unsigned short)value __attribute__((unavailable));
+ (instancetype)numberWithInt:(int)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedInt:(unsigned int)value __attribute__((unavailable));
+ (instancetype)numberWithLong:(long)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedLong:(unsigned long)value __attribute__((unavailable));
+ (instancetype)numberWithLongLong:(long long)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedLongLong:(unsigned long long)value __attribute__((unavailable));
+ (instancetype)numberWithFloat:(float)value __attribute__((unavailable));
+ (instancetype)numberWithDouble:(double)value __attribute__((unavailable));
+ (instancetype)numberWithBool:(BOOL)value __attribute__((unavailable));
+ (instancetype)numberWithInteger:(NSInteger)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedInteger:(NSUInteger)value __attribute__((unavailable));
@end

__attribute__((swift_name("KotlinByte")))
@interface ComposeAppByte : ComposeAppNumber
- (instancetype)initWithChar:(char)value;
+ (instancetype)numberWithChar:(char)value;
@end

__attribute__((swift_name("KotlinUByte")))
@interface ComposeAppUByte : ComposeAppNumber
- (instancetype)initWithUnsignedChar:(unsigned char)value;
+ (instancetype)numberWithUnsignedChar:(unsigned char)value;
@end

__attribute__((swift_name("KotlinShort")))
@interface ComposeAppShort : ComposeAppNumber
- (instancetype)initWithShort:(short)value;
+ (instancetype)numberWithShort:(short)value;
@end

__attribute__((swift_name("KotlinUShort")))
@interface ComposeAppUShort : ComposeAppNumber
- (instancetype)initWithUnsignedShort:(unsigned short)value;
+ (instancetype)numberWithUnsignedShort:(unsigned short)value;
@end

__attribute__((swift_name("KotlinInt")))
@interface ComposeAppInt : ComposeAppNumber
- (instancetype)initWithInt:(int)value;
+ (instancetype)numberWithInt:(int)value;
@end

__attribute__((swift_name("KotlinUInt")))
@interface ComposeAppUInt : ComposeAppNumber
- (instancetype)initWithUnsignedInt:(unsigned int)value;
+ (instancetype)numberWithUnsignedInt:(unsigned int)value;
@end

__attribute__((swift_name("KotlinLong")))
@interface ComposeAppLong : ComposeAppNumber
- (instancetype)initWithLongLong:(long long)value;
+ (instancetype)numberWithLongLong:(long long)value;
@end

__attribute__((swift_name("KotlinULong")))
@interface ComposeAppULong : ComposeAppNumber
- (instancetype)initWithUnsignedLongLong:(unsigned long long)value;
+ (instancetype)numberWithUnsignedLongLong:(unsigned long long)value;
@end

__attribute__((swift_name("KotlinFloat")))
@interface ComposeAppFloat : ComposeAppNumber
- (instancetype)initWithFloat:(float)value;
+ (instancetype)numberWithFloat:(float)value;
@end

__attribute__((swift_name("KotlinDouble")))
@interface ComposeAppDouble : ComposeAppNumber
- (instancetype)initWithDouble:(double)value;
+ (instancetype)numberWithDouble:(double)value;
@end

__attribute__((swift_name("KotlinBoolean")))
@interface ComposeAppBoolean : ComposeAppNumber
- (instancetype)initWithBool:(BOOL)value;
+ (instancetype)numberWithBool:(BOOL)value;
@end


/**
 * Convenience entry point for creating and configuring an SDK instance.
 */
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Deepdots")))
@interface ComposeAppDeepdots : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));

/**
 * Convenience entry point for creating and configuring an SDK instance.
 */
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)deepdots __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppDeepdots *shared __attribute__((swift_name("shared")));

/** Creates an empty instance (you must call `init` separately). */
- (ComposeAppDeepdotsPopups *)create __attribute__((swift_name("create()")));

/** Creates and initializes an instance in a single step. */
- (ComposeAppDeepdotsPopups *)createInitializedOptions:(ComposeAppInitOptions *)options __attribute__((swift_name("createInitialized(options:)")));

/**
 * Simple helper for Swift/ObjC interop: create and init SDK with a single popup without referencing Kotlin data classes from Swift.
 */
- (ComposeAppDeepdotsPopups *)createInitializedSimpleId:(NSString *)id title:(NSString *)title messageHtml:(NSString *)messageHtml surveyId:(NSString *)surveyId productId:(NSString *)productId triggerSeconds:(int32_t)triggerSeconds acceptLabel:(NSString *)acceptLabel declineLabel:(NSString *)declineLabel declineCooldownDays:(int32_t)declineCooldownDays debug:(BOOL)debug autoLaunch:(BOOL)autoLaunch lang:(NSString * _Nullable)lang path:(NSString * _Nullable)path __attribute__((swift_name("createInitializedSimple(id:title:messageHtml:surveyId:productId:triggerSeconds:acceptLabel:declineLabel:declineCooldownDays:debug:autoLaunch:lang:path:)")));

/** Manually dismisses the active popup (re-export). */
- (void)dismissContext:(ComposeAppPlatformContext *)context __attribute__((swift_name("dismiss(context:)")));

/**
 * Returns the full HTML used to render a MagicFeedback survey (loader + CDN fallback included).
 * Useful for hosts that want to load it directly inside an Android WebView or iOS WKWebView.
 */
- (NSString *)getSurveyHtmlSurveyId:(NSString *)surveyId productId:(NSString *)productId __attribute__((swift_name("getSurveyHtml(surveyId:productId:)")));

/** Current epoch millis (re-export). */
- (int64_t)now __attribute__((swift_name("now()")));

/** Basic popup HTML parser (re-export). */
- (NSArray<ComposeAppHtmlParagraph *> *)parseHtmlHtml:(NSString *)html __attribute__((swift_name("parseHtml(html:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("DeepdotsPopups")))
@interface ComposeAppDeepdotsPopups : ComposeAppBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)attachContextContext:(ComposeAppPlatformContext *)context __attribute__((swift_name("attachContext(context:)")));
- (void)autoLaunch __attribute__((swift_name("autoLaunch()")));
- (void)closeContext:(ComposeAppPlatformContext *)context __attribute__((swift_name("close(context:)")));
- (NSArray<ComposeAppPopupDefinition *> *)debugListPopups __attribute__((swift_name("debugListPopups()")));
- (void)doInitOptions:(ComposeAppInitOptions *)options __attribute__((swift_name("doInit(options:)")));
- (void)initializeOptions:(ComposeAppInitOptions *)options __attribute__((swift_name("initialize(options:)")));

/**
 * Records that a survey has been completed for the current user.
 *
 * You normally do **not** need to call this from host code: the SDK invokes it
 * automatically when the underlying survey emits `survey_completed` or when the
 * user taps the "Complete" action. It is exposed publicly only for unusual
 * integrations that bypass the normal completion flow.
 */
- (void)markSurveyAnsweredSurveyId:(NSString *)surveyId __attribute__((swift_name("markSurveyAnswered(surveyId:)")));
- (void)offEvent:(ComposeAppEvent *)event listener:(void (^)(ComposeAppEventData *))listener __attribute__((swift_name("off(event:listener:)")));
- (void)onEvent:(ComposeAppEvent *)event listener:(void (^)(ComposeAppEventData *))listener __attribute__((swift_name("on(event:listener:)")));
- (void)onExit __attribute__((swift_name("onExit()")));
- (void)onScrollPercentage:(int32_t)percentage __attribute__((swift_name("onScroll(percentage:)")));
- (void)setPathPath:(NSString * _Nullable)path __attribute__((swift_name("setPath(path:)")));
- (void)showOptions:(ComposeAppShowOptions *)options context:(ComposeAppPlatformContext *)context __attribute__((swift_name("show(options:context:)")));
- (void)showByPopupIdPopupId:(NSString *)popupId context:(ComposeAppPlatformContext *)context __attribute__((swift_name("showByPopupId(popupId:context:)")));
- (void)surveyCompletedFromJsSurveyId:(NSString *)surveyId __attribute__((swift_name("surveyCompletedFromJs(surveyId:)")));
- (void)triggerClickTargetId:(NSString *)targetId __attribute__((swift_name("triggerClick(targetId:)")));
- (void)triggerEventEventName:(NSString *)eventName __attribute__((swift_name("triggerEvent(eventName:)")));
- (void)triggerSurveySurveyId:(NSString *)surveyId context:(ComposeAppPlatformContext *)context popupId:(NSString * _Nullable)popupId __attribute__((swift_name("triggerSurvey(surveyId:context:popupId:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("EventBus")))
@interface ComposeAppEventBus : ComposeAppBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));

/** Clears every registered listener.
 *
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)clearWithCompletionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("clear(completionHandler:)")));

/** Emits an event to all registered listeners (thread-safe).
 *
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)emitEvent:(ComposeAppEvent *)event data:(ComposeAppEventData *)data completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("emit(event:data:completionHandler:)")));

/** Removes a previously registered listener.
 *
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)offEvent:(ComposeAppEvent *)event listener:(void (^)(ComposeAppEventData *))listener completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("off(event:listener:completionHandler:)")));

/** Registers a listener for the given event (thread-safe).
 *
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)onEvent:(ComposeAppEvent *)event listener:(void (^)(ComposeAppEventData *))listener completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("on(event:listener:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Events")))
@interface ComposeAppEvents : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)events __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppEvents *shared __attribute__((swift_name("shared")));
@property (readonly) ComposeAppEvent *popupClicked __attribute__((swift_name("popupClicked")));
@property (readonly) ComposeAppEvent *popupShown __attribute__((swift_name("popupShown")));
@property (readonly) ComposeAppEvent *surveyCompleted __attribute__((swift_name("surveyCompleted")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Greeting")))
@interface ComposeAppGreeting : ComposeAppBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (NSString *)greet __attribute__((swift_name("greet()")));
@end

__attribute__((swift_name("Platform")))
@protocol ComposeAppPlatform
@required
@property (readonly) NSString *name __attribute__((swift_name("name")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("IOSPlatform")))
@interface ComposeAppIOSPlatform : ComposeAppBase <ComposeAppPlatform>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@end


/**
 * Simple runtime holder for SDK-wide config needed by UI/HTML builders.
 */
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SdkRuntime")))
@interface ComposeAppSdkRuntime : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));

/**
 * Simple runtime holder for SDK-wide config needed by UI/HTML builders.
 */
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)sdkRuntime __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppSdkRuntime *shared __attribute__((swift_name("shared")));
@property NSString *env __attribute__((swift_name("env")));
@property NSDictionary<NSString *, id> * _Nullable metadata __attribute__((swift_name("metadata")));
@property NSString * _Nullable publicKey __attribute__((swift_name("publicKey")));
@property NSString * _Nullable userId __attribute__((swift_name("userId")));
@end

__attribute__((swift_name("Action")))
@interface ComposeAppAction : ComposeAppBase
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Action.Accept")))
@interface ComposeAppActionAccept : ComposeAppAction
- (instancetype)initWithLabel:(NSString *)label surveyId:(NSString *)surveyId __attribute__((swift_name("init(label:surveyId:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppActionAccept *)doCopyLabel:(NSString *)label surveyId:(NSString *)surveyId __attribute__((swift_name("doCopy(label:surveyId:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *label __attribute__((swift_name("label")));
@property (readonly) NSString *surveyId __attribute__((swift_name("surveyId")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Action.Back")))
@interface ComposeAppActionBack : ComposeAppAction
- (instancetype)initWithLabel:(NSString *)label __attribute__((swift_name("init(label:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppActionBack *)doCopyLabel:(NSString *)label __attribute__((swift_name("doCopy(label:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *label __attribute__((swift_name("label")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Action.Complete")))
@interface ComposeAppActionComplete : ComposeAppAction
- (instancetype)initWithLabel:(NSString *)label __attribute__((swift_name("init(label:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppActionComplete *)doCopyLabel:(NSString *)label __attribute__((swift_name("doCopy(label:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *label __attribute__((swift_name("label")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Action.Decline")))
@interface ComposeAppActionDecline : ComposeAppAction
- (instancetype)initWithLabel:(NSString *)label cooldownDays:(int32_t)cooldownDays __attribute__((swift_name("init(label:cooldownDays:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppActionDecline *)doCopyLabel:(NSString *)label cooldownDays:(int32_t)cooldownDays __attribute__((swift_name("doCopy(label:cooldownDays:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t cooldownDays __attribute__((swift_name("cooldownDays")));
@property (readonly) NSString *label __attribute__((swift_name("label")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Action.Start")))
@interface ComposeAppActionStart : ComposeAppAction
- (instancetype)initWithLabel:(NSString *)label __attribute__((swift_name("init(label:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppActionStart *)doCopyLabel:(NSString *)label __attribute__((swift_name("doCopy(label:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *label __attribute__((swift_name("label")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Actions")))
@interface ComposeAppActions : ComposeAppBase
- (instancetype)initWithAccept:(ComposeAppActionAccept * _Nullable)accept decline:(ComposeAppActionDecline * _Nullable)decline start:(ComposeAppActionStart * _Nullable)start complete:(ComposeAppActionComplete * _Nullable)complete back:(ComposeAppActionBack * _Nullable)back __attribute__((swift_name("init(accept:decline:start:complete:back:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppActions *)doCopyAccept:(ComposeAppActionAccept * _Nullable)accept decline:(ComposeAppActionDecline * _Nullable)decline start:(ComposeAppActionStart * _Nullable)start complete:(ComposeAppActionComplete * _Nullable)complete back:(ComposeAppActionBack * _Nullable)back __attribute__((swift_name("doCopy(accept:decline:start:complete:back:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) ComposeAppActionAccept * _Nullable accept __attribute__((swift_name("accept")));
@property (readonly) ComposeAppActionBack * _Nullable back __attribute__((swift_name("back")));
@property (readonly) ComposeAppActionComplete * _Nullable complete __attribute__((swift_name("complete")));
@property (readonly) ComposeAppActionDecline * _Nullable decline __attribute__((swift_name("decline")));
@property (readonly) ComposeAppActionStart * _Nullable start __attribute__((swift_name("start")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CooldownCondition")))
@interface ComposeAppCooldownCondition : ComposeAppBase
- (instancetype)initWithAnswered:(ComposeAppTriggerConditionStatus *)answered cooldownDays:(int32_t)cooldownDays __attribute__((swift_name("init(answered:cooldownDays:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppCooldownCondition *)doCopyAnswered:(ComposeAppTriggerConditionStatus *)answered cooldownDays:(int32_t)cooldownDays __attribute__((swift_name("doCopy(answered:cooldownDays:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) ComposeAppTriggerConditionStatus *answered __attribute__((swift_name("answered")));
@property (readonly) int32_t cooldownDays __attribute__((swift_name("cooldownDays")));
@end

__attribute__((swift_name("KotlinComparable")))
@protocol ComposeAppKotlinComparable
@required
- (int32_t)compareToOther:(id _Nullable)other __attribute__((swift_name("compareTo(other:)")));
@end

__attribute__((swift_name("KotlinEnum")))
@interface ComposeAppKotlinEnum<E> : ComposeAppBase <ComposeAppKotlinComparable>
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) ComposeAppKotlinEnumCompanion *companion __attribute__((swift_name("companion")));
- (int32_t)compareToOther:(E)other __attribute__((swift_name("compareTo(other:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@property (readonly) int32_t ordinal __attribute__((swift_name("ordinal")));
@end


/**
 * Backend environment the SDK should talk to.
 *
 * Defaults to [Production] (https://api.deepdots.com). Set [Development] to point
 * the SDK at https://api-dev.deepdots.com. This is independent of [InitOptions.debug],
 * which only controls SDK log output.
 */
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Environment")))
@interface ComposeAppEnvironment : ComposeAppKotlinEnum<ComposeAppEnvironment *>
+ (instancetype)alloc __attribute__((unavailable));

/**
 * Backend environment the SDK should talk to.
 *
 * Defaults to [Production] (https://api.deepdots.com). Set [Development] to point
 * the SDK at https://api-dev.deepdots.com. This is independent of [InitOptions.debug],
 * which only controls SDK log output.
 */
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) ComposeAppEnvironment *production __attribute__((swift_name("production")));
@property (class, readonly) ComposeAppEnvironment *development __attribute__((swift_name("development")));
+ (ComposeAppKotlinArray<ComposeAppEnvironment *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<ComposeAppEnvironment *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Event")))
@interface ComposeAppEvent : ComposeAppKotlinEnum<ComposeAppEvent *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) ComposeAppEvent *popupshown __attribute__((swift_name("popupshown")));
@property (class, readonly) ComposeAppEvent *popupclicked __attribute__((swift_name("popupclicked")));
@property (class, readonly) ComposeAppEvent *surveycompleted __attribute__((swift_name("surveycompleted")));
+ (ComposeAppKotlinArray<ComposeAppEvent *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<ComposeAppEvent *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("EventData")))
@interface ComposeAppEventData : ComposeAppBase
- (instancetype)initWithPopupId:(NSString *)popupId surveyId:(NSString *)surveyId productId:(NSString *)productId extra:(NSDictionary<NSString *, id> *)extra timestamp:(int64_t)timestamp __attribute__((swift_name("init(popupId:surveyId:productId:extra:timestamp:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppEventData *)doCopyPopupId:(NSString *)popupId surveyId:(NSString *)surveyId productId:(NSString *)productId extra:(NSDictionary<NSString *, id> *)extra timestamp:(int64_t)timestamp __attribute__((swift_name("doCopy(popupId:surveyId:productId:extra:timestamp:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSDictionary<NSString *, id> *extra __attribute__((swift_name("extra")));
@property (readonly) NSString *popupId __attribute__((swift_name("popupId")));
@property (readonly) NSString *productId __attribute__((swift_name("productId")));
@property (readonly) NSString *surveyId __attribute__((swift_name("surveyId")));
@property (readonly) int64_t timestamp __attribute__((swift_name("timestamp")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ImageAlignment")))
@interface ComposeAppImageAlignment : ComposeAppKotlinEnum<ComposeAppImageAlignment *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) ComposeAppImageAlignment *center __attribute__((swift_name("center")));
@property (class, readonly) ComposeAppImageAlignment *left __attribute__((swift_name("left")));
@property (class, readonly) ComposeAppImageAlignment *right __attribute__((swift_name("right")));
+ (ComposeAppKotlinArray<ComposeAppImageAlignment *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<ComposeAppImageAlignment *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ImageSize")))
@interface ComposeAppImageSize : ComposeAppKotlinEnum<ComposeAppImageSize *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) ComposeAppImageSize *small __attribute__((swift_name("small")));
@property (class, readonly) ComposeAppImageSize *medium __attribute__((swift_name("medium")));
@property (class, readonly) ComposeAppImageSize *large __attribute__((swift_name("large")));
+ (ComposeAppKotlinArray<ComposeAppImageSize *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<ComposeAppImageSize *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("InitOptions")))
@interface ComposeAppInitOptions : ComposeAppBase
- (instancetype)initWithDebug:(ComposeAppBoolean * _Nullable)debug environment:(ComposeAppEnvironment * _Nullable)environment mode:(ComposeAppMode * _Nullable)mode popupOptions:(ComposeAppPopupOptions *)popupOptions provideLang:(NSString * _Nullable (^)(void))provideLang autoLaunch:(ComposeAppBoolean * _Nullable)autoLaunch storage:(id<ComposeAppKeyValueStorage> _Nullable)storage metadata:(NSDictionary<NSString *, id> * _Nullable)metadata __attribute__((swift_name("init(debug:environment:mode:popupOptions:provideLang:autoLaunch:storage:metadata:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppInitOptions *)doCopyDebug:(ComposeAppBoolean * _Nullable)debug environment:(ComposeAppEnvironment * _Nullable)environment mode:(ComposeAppMode * _Nullable)mode popupOptions:(ComposeAppPopupOptions *)popupOptions provideLang:(NSString * _Nullable (^)(void))provideLang autoLaunch:(ComposeAppBoolean * _Nullable)autoLaunch storage:(id<ComposeAppKeyValueStorage> _Nullable)storage metadata:(NSDictionary<NSString *, id> * _Nullable)metadata __attribute__((swift_name("doCopy(debug:environment:mode:popupOptions:provideLang:autoLaunch:storage:metadata:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) ComposeAppBoolean * _Nullable autoLaunch __attribute__((swift_name("autoLaunch")));
@property (readonly) ComposeAppBoolean * _Nullable debug __attribute__((swift_name("debug")));
@property (readonly) ComposeAppEnvironment * _Nullable environment __attribute__((swift_name("environment")));
@property (readonly) NSDictionary<NSString *, id> * _Nullable metadata __attribute__((swift_name("metadata")));
@property (readonly) ComposeAppMode * _Nullable mode __attribute__((swift_name("mode")));
@property (readonly) ComposeAppPopupOptions *popupOptions __attribute__((swift_name("popupOptions")));
@property (readonly) NSString * _Nullable (^provideLang)(void) __attribute__((swift_name("provideLang")));
@property (readonly) id<ComposeAppKeyValueStorage> _Nullable storage __attribute__((swift_name("storage")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("LegacyCondition")))
@interface ComposeAppLegacyCondition : ComposeAppBase
- (instancetype)initWithAnswered:(ComposeAppBoolean * _Nullable)answered cooldownDays:(int32_t)cooldownDays __attribute__((swift_name("init(answered:cooldownDays:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppLegacyCondition *)doCopyAnswered:(ComposeAppBoolean * _Nullable)answered cooldownDays:(int32_t)cooldownDays __attribute__((swift_name("doCopy(answered:cooldownDays:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) ComposeAppBoolean * _Nullable answered __attribute__((swift_name("answered")));
@property (readonly) int32_t cooldownDays __attribute__((swift_name("cooldownDays")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Mode")))
@interface ComposeAppMode : ComposeAppKotlinEnum<ComposeAppMode *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) ComposeAppMode *client __attribute__((swift_name("client")));
@property (class, readonly) ComposeAppMode *server __attribute__((swift_name("server")));
+ (ComposeAppKotlinArray<ComposeAppMode *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<ComposeAppMode *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PopupDefinition")))
@interface ComposeAppPopupDefinition : ComposeAppBase
- (instancetype)initWithId:(NSString *)id title:(NSString *)title message:(NSString *)message trigger:(ComposeAppTrigger * _Nullable)trigger triggers:(NSArray<ComposeAppTrigger *> *)triggers conditions:(NSArray<ComposeAppLegacyCondition *> *)conditions cooldown:(NSArray<ComposeAppCooldownCondition *> *)cooldown legacyConditions:(NSArray<ComposeAppLegacyCondition *> *)legacyConditions actions:(ComposeAppActions *)actions surveyId:(NSString *)surveyId productId:(NSString *)productId style:(ComposeAppStyle *)style segments:(ComposeAppSegments * _Nullable)segments __attribute__((swift_name("init(id:title:message:trigger:triggers:conditions:cooldown:legacyConditions:actions:surveyId:productId:style:segments:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppPopupDefinition *)doCopyId:(NSString *)id title:(NSString *)title message:(NSString *)message trigger:(ComposeAppTrigger * _Nullable)trigger triggers:(NSArray<ComposeAppTrigger *> *)triggers conditions:(NSArray<ComposeAppLegacyCondition *> *)conditions cooldown:(NSArray<ComposeAppCooldownCondition *> *)cooldown legacyConditions:(NSArray<ComposeAppLegacyCondition *> *)legacyConditions actions:(ComposeAppActions *)actions surveyId:(NSString *)surveyId productId:(NSString *)productId style:(ComposeAppStyle *)style segments:(ComposeAppSegments * _Nullable)segments __attribute__((swift_name("doCopy(id:title:message:trigger:triggers:conditions:cooldown:legacyConditions:actions:surveyId:productId:style:segments:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) ComposeAppActions *actions __attribute__((swift_name("actions")));
@property (readonly) NSArray<ComposeAppLegacyCondition *> *conditions __attribute__((swift_name("conditions")));
@property (readonly) NSArray<ComposeAppCooldownCondition *> *cooldown __attribute__((swift_name("cooldown")));
@property (readonly) NSString *id __attribute__((swift_name("id")));
@property (readonly) NSArray<ComposeAppLegacyCondition *> *legacyConditions __attribute__((swift_name("legacyConditions")));
@property (readonly) NSString *message __attribute__((swift_name("message")));
@property (readonly) NSString *productId __attribute__((swift_name("productId")));
@property (readonly) ComposeAppSegments * _Nullable segments __attribute__((swift_name("segments")));
@property (readonly) ComposeAppStyle *style __attribute__((swift_name("style")));
@property (readonly) NSString *surveyId __attribute__((swift_name("surveyId")));
@property (readonly) NSString *title __attribute__((swift_name("title")));
@property (readonly) ComposeAppTrigger * _Nullable trigger __attribute__((swift_name("trigger")));
@property (readonly) NSArray<ComposeAppTrigger *> *triggers __attribute__((swift_name("triggers")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PopupOptions")))
@interface ComposeAppPopupOptions : ComposeAppBase
- (instancetype)initWithId:(NSString * _Nullable)id publicKey:(NSString * _Nullable)publicKey popups:(NSArray<ComposeAppPopupDefinition *> * _Nullable)popups companyId:(NSString * _Nullable)companyId __attribute__((swift_name("init(id:publicKey:popups:companyId:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppPopupOptions *)doCopyId:(NSString * _Nullable)id publicKey:(NSString * _Nullable)publicKey popups:(NSArray<ComposeAppPopupDefinition *> * _Nullable)popups companyId:(NSString * _Nullable)companyId __attribute__((swift_name("doCopy(id:publicKey:popups:companyId:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString * _Nullable companyId __attribute__((swift_name("companyId")));
@property (readonly) NSString * _Nullable id __attribute__((swift_name("id")));
@property (readonly) NSArray<ComposeAppPopupDefinition *> * _Nullable popups __attribute__((swift_name("popups")));
@property (readonly) NSString * _Nullable publicKey __attribute__((swift_name("publicKey")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Position")))
@interface ComposeAppPosition : ComposeAppKotlinEnum<ComposeAppPosition *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) ComposeAppPosition *topleft __attribute__((swift_name("topleft")));
@property (class, readonly) ComposeAppPosition *topright __attribute__((swift_name("topright")));
@property (class, readonly) ComposeAppPosition *bottomleft __attribute__((swift_name("bottomleft")));
@property (class, readonly) ComposeAppPosition *bottomright __attribute__((swift_name("bottomright")));
@property (class, readonly) ComposeAppPosition *center __attribute__((swift_name("center")));
+ (ComposeAppKotlinArray<ComposeAppPosition *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<ComposeAppPosition *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Segments")))
@interface ComposeAppSegments : ComposeAppBase
- (instancetype)initWithLang:(NSArray<NSString *> *)lang path:(NSArray<NSString *> *)path __attribute__((swift_name("init(lang:path:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppSegments *)doCopyLang:(NSArray<NSString *> *)lang path:(NSArray<NSString *> *)path __attribute__((swift_name("doCopy(lang:path:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSArray<NSString *> *lang __attribute__((swift_name("lang")));
@property (readonly) NSArray<NSString *> *path __attribute__((swift_name("path")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ShowOptions")))
@interface ComposeAppShowOptions : ComposeAppBase
- (instancetype)initWithSurveyId:(NSString *)surveyId productId:(NSString *)productId data:(NSDictionary<NSString *, id> * _Nullable)data __attribute__((swift_name("init(surveyId:productId:data:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppShowOptions *)doCopySurveyId:(NSString *)surveyId productId:(NSString *)productId data:(NSDictionary<NSString *, id> * _Nullable)data __attribute__((swift_name("doCopy(surveyId:productId:data:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSDictionary<NSString *, id> * _Nullable data __attribute__((swift_name("data")));
@property (readonly) NSString *productId __attribute__((swift_name("productId")));
@property (readonly) NSString *surveyId __attribute__((swift_name("surveyId")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Style")))
@interface ComposeAppStyle : ComposeAppBase
- (instancetype)initWithTheme:(ComposeAppTheme *)theme position:(ComposeAppPosition *)position imageUrl:(NSString * _Nullable)imageUrl imageSize:(ComposeAppImageSize *)imageSize imageAlignment:(ComposeAppImageAlignment *)imageAlignment __attribute__((swift_name("init(theme:position:imageUrl:imageSize:imageAlignment:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppStyle *)doCopyTheme:(ComposeAppTheme *)theme position:(ComposeAppPosition *)position imageUrl:(NSString * _Nullable)imageUrl imageSize:(ComposeAppImageSize *)imageSize imageAlignment:(ComposeAppImageAlignment *)imageAlignment __attribute__((swift_name("doCopy(theme:position:imageUrl:imageSize:imageAlignment:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) ComposeAppImageAlignment *imageAlignment __attribute__((swift_name("imageAlignment")));
@property (readonly) ComposeAppImageSize *imageSize __attribute__((swift_name("imageSize")));
@property (readonly) NSString * _Nullable imageUrl __attribute__((swift_name("imageUrl")));
@property (readonly) ComposeAppPosition *position __attribute__((swift_name("position")));
@property (readonly) ComposeAppTheme *theme __attribute__((swift_name("theme")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SurveyProgressState")))
@interface ComposeAppSurveyProgressState : ComposeAppBase
- (instancetype)initWithStatus:(ComposeAppTriggerConditionStatus *)status timestamp:(int64_t)timestamp __attribute__((swift_name("init(status:timestamp:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppSurveyProgressState *)doCopyStatus:(ComposeAppTriggerConditionStatus *)status timestamp:(int64_t)timestamp __attribute__((swift_name("doCopy(status:timestamp:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) ComposeAppTriggerConditionStatus *status __attribute__((swift_name("status")));
@property (readonly) int64_t timestamp __attribute__((swift_name("timestamp")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Theme")))
@interface ComposeAppTheme : ComposeAppKotlinEnum<ComposeAppTheme *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) ComposeAppTheme *light __attribute__((swift_name("light")));
@property (class, readonly) ComposeAppTheme *dark __attribute__((swift_name("dark")));
+ (ComposeAppKotlinArray<ComposeAppTheme *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<ComposeAppTheme *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((swift_name("Trigger")))
@interface ComposeAppTrigger : ComposeAppBase
@property (readonly) NSString * _Nullable rawValue __attribute__((swift_name("rawValue")));
@property (readonly) ComposeAppTriggerType *type __attribute__((swift_name("type")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Trigger.Click")))
@interface ComposeAppTriggerClick : ComposeAppTrigger
- (instancetype)initWithTargetId:(NSString *)targetId __attribute__((swift_name("init(targetId:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppTriggerClick *)doCopyTargetId:(NSString *)targetId __attribute__((swift_name("doCopy(targetId:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *rawValue __attribute__((swift_name("rawValue")));
@property (readonly) NSString *targetId __attribute__((swift_name("targetId")));
@property (readonly) ComposeAppTriggerType *type __attribute__((swift_name("type")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Trigger.Event")))
@interface ComposeAppTriggerEvent : ComposeAppTrigger
- (instancetype)initWithName:(NSString *)name __attribute__((swift_name("init(name:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppTriggerEvent *)doCopyName:(NSString *)name __attribute__((swift_name("doCopy(name:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@property (readonly) NSString *rawValue __attribute__((swift_name("rawValue")));
@property (readonly) ComposeAppTriggerType *type __attribute__((swift_name("type")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Trigger.Exit")))
@interface ComposeAppTriggerExit : ComposeAppTrigger
- (instancetype)initWithDelaySeconds:(double)delaySeconds __attribute__((swift_name("init(delaySeconds:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppTriggerExit *)doCopyDelaySeconds:(double)delaySeconds __attribute__((swift_name("doCopy(delaySeconds:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) double delaySeconds __attribute__((swift_name("delaySeconds")));
@property (readonly) NSString *rawValue __attribute__((swift_name("rawValue")));
@property (readonly) ComposeAppTriggerType *type __attribute__((swift_name("type")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Trigger.Scroll")))
@interface ComposeAppTriggerScroll : ComposeAppTrigger
- (instancetype)initWithPercentage:(int32_t)percentage __attribute__((swift_name("init(percentage:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppTriggerScroll *)doCopyPercentage:(int32_t)percentage __attribute__((swift_name("doCopy(percentage:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t percentage __attribute__((swift_name("percentage")));
@property (readonly) NSString *rawValue __attribute__((swift_name("rawValue")));
@property (readonly) ComposeAppTriggerType *type __attribute__((swift_name("type")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Trigger.TimeOnPage")))
@interface ComposeAppTriggerTimeOnPage : ComposeAppTrigger
- (instancetype)initWithSeconds:(double)seconds __attribute__((swift_name("init(seconds:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppTriggerTimeOnPage *)doCopySeconds:(double)seconds __attribute__((swift_name("doCopy(seconds:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *rawValue __attribute__((swift_name("rawValue")));
@property (readonly) double seconds __attribute__((swift_name("seconds")));
@property (readonly) ComposeAppTriggerType *type __attribute__((swift_name("type")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TriggerConditionStatus")))
@interface ComposeAppTriggerConditionStatus : ComposeAppKotlinEnum<ComposeAppTriggerConditionStatus *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) ComposeAppTriggerConditionStatus *showed __attribute__((swift_name("showed")));
@property (class, readonly) ComposeAppTriggerConditionStatus *partial __attribute__((swift_name("partial")));
@property (class, readonly) ComposeAppTriggerConditionStatus *completed __attribute__((swift_name("completed")));
+ (ComposeAppKotlinArray<ComposeAppTriggerConditionStatus *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<ComposeAppTriggerConditionStatus *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TriggerType")))
@interface ComposeAppTriggerType : ComposeAppKotlinEnum<ComposeAppTriggerType *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) ComposeAppTriggerType *timeonpage __attribute__((swift_name("timeonpage")));
@property (class, readonly) ComposeAppTriggerType *scroll __attribute__((swift_name("scroll")));
@property (class, readonly) ComposeAppTriggerType *exit __attribute__((swift_name("exit")));
@property (class, readonly) ComposeAppTriggerType *event __attribute__((swift_name("event")));
@property (class, readonly) ComposeAppTriggerType *click __attribute__((swift_name("click")));
+ (ComposeAppKotlinArray<ComposeAppTriggerType *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<ComposeAppTriggerType *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PlatformContext")))
@interface ComposeAppPlatformContext : ComposeAppBase
- (instancetype)initWithViewController:(UIViewController *)viewController __attribute__((swift_name("init(viewController:)"))) __attribute__((objc_designated_initializer));
@property (readonly) UIViewController *viewController __attribute__((swift_name("viewController")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PopupRenderer")))
@interface ComposeAppPopupRenderer : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)popupRenderer __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppPopupRenderer *shared __attribute__((swift_name("shared")));
- (void)showPopup:(ComposeAppPopupDefinition *)popup context:(ComposeAppPlatformContext *)context onAction:(void (^)(ComposeAppAction *))onAction onSurveyEvent:(void (^)(NSString *, NSString * _Nullable))onSurveyEvent onDismiss:(void (^)(void))onDismiss __attribute__((swift_name("show(popup:context:onAction:onSurveyEvent:onDismiss:)")));
@end


/**
 * Service to handle network calls related to popups.
 */
__attribute__((swift_name("PopupsService")))
@protocol ComposeAppPopupsService
@required

/**
 * Fetches popups JSON from the server for the current environment and public key.
 * Base URL is derived from SdkRuntime.env ("dev" -> api-dev, otherwise api).
 * @param publicKey project public key
 * @param filter optional LoopBack-style filter JSON string
 * @return raw response body as text (JSON array)
 *
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)fetchPopupsPublicKey:(NSString *)publicKey filter:(NSString * _Nullable)filter completionHandler:(void (^)(NSString * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("fetchPopups(publicKey:filter:completionHandler:)")));

/**
 * Posts a popup event to the server.
 * Endpoint: POST /sdk/popups
 *
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)postPopupEventPublicKey:(NSString *)publicKey status:(NSString *)status popupId:(NSString *)popupId userId:(NSString * _Nullable)userId completionHandler:(void (^)(NSString * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("postPopupEvent(publicKey:status:popupId:userId:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("DefaultPopupsService")))
@interface ComposeAppDefaultPopupsService : ComposeAppBase <ComposeAppPopupsService>
- (instancetype)initWithHttpClient:(ComposeAppKtor_client_coreHttpClient *)httpClient __attribute__((swift_name("init(httpClient:)"))) __attribute__((objc_designated_initializer));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)fetchPopupsPublicKey:(NSString *)publicKey filter:(NSString * _Nullable)filter completionHandler:(void (^)(NSString * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("fetchPopups(publicKey:filter:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)postPopupEventPublicKey:(NSString *)publicKey status:(NSString *)status popupId:(NSString *)popupId userId:(NSString * _Nullable)userId completionHandler:(void (^)(NSString * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("postPopupEvent(publicKey:status:popupId:userId:completionHandler:)")));
@end


/** Lightweight cross-platform key/value store used internally for cooldown caching. */
__attribute__((swift_name("KeyValueStorage")))
@protocol ComposeAppKeyValueStorage
@required
- (ComposeAppLong * _Nullable)getLongKey:(NSString *)key __attribute__((swift_name("getLong(key:)")));
- (NSString * _Nullable)getStringKey:(NSString *)key __attribute__((swift_name("getString(key:)")));
- (void)putLongKey:(NSString *)key value:(int64_t)value __attribute__((swift_name("putLong(key:value:)")));
- (void)putStringKey:(NSString *)key value:(NSString *)value __attribute__((swift_name("putString(key:value:)")));
- (void)removeKey:(NSString *)key __attribute__((swift_name("remove(key:)")));
@end


/** Implementación en memoria (fallback si la plataforma no provee storage real) */
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("InMemoryStorage")))
@interface ComposeAppInMemoryStorage : ComposeAppBase <ComposeAppKeyValueStorage>

/** Implementación en memoria (fallback si la plataforma no provee storage real) */
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));

/** Implementación en memoria (fallback si la plataforma no provee storage real) */
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (ComposeAppLong * _Nullable)getLongKey:(NSString *)key __attribute__((swift_name("getLong(key:)")));
- (NSString * _Nullable)getStringKey:(NSString *)key __attribute__((swift_name("getString(key:)")));
- (void)putLongKey:(NSString *)key value:(int64_t)value __attribute__((swift_name("putLong(key:value:)")));
- (void)putStringKey:(NSString *)key value:(NSString *)value __attribute__((swift_name("putString(key:value:)")));
- (void)removeKey:(NSString *)key __attribute__((swift_name("remove(key:)")));
@end

__attribute__((swift_name("SurveyController")))
@protocol ComposeAppSurveyController
@required
- (void)back __attribute__((swift_name("back()")));
- (void)close __attribute__((swift_name("close()")));
- (void)send __attribute__((swift_name("send()")));
- (void)startForm __attribute__((swift_name("startForm()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("HtmlParagraph")))
@interface ComposeAppHtmlParagraph : ComposeAppBase
- (instancetype)initWithRuns:(NSArray<ComposeAppHtmlRun *> *)runs __attribute__((swift_name("init(runs:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppHtmlParagraph *)doCopyRuns:(NSArray<ComposeAppHtmlRun *> *)runs __attribute__((swift_name("doCopy(runs:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSArray<ComposeAppHtmlRun *> *runs __attribute__((swift_name("runs")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("HtmlRun")))
@interface ComposeAppHtmlRun : ComposeAppBase
- (instancetype)initWithText:(NSString *)text bold:(BOOL)bold italic:(BOOL)italic __attribute__((swift_name("init(text:bold:italic:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppHtmlRun *)doCopyText:(NSString *)text bold:(BOOL)bold italic:(BOOL)italic __attribute__((swift_name("doCopy(text:bold:italic:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) BOOL bold __attribute__((swift_name("bold")));
@property (readonly) BOOL italic __attribute__((swift_name("italic")));
@property (readonly) NSString *text __attribute__((swift_name("text")));
@end

@interface ComposeAppDeepdotsPopups (Extensions)
- (void (^)(void))bindScrollViewScrollView:(id)scrollView __attribute__((swift_name("bindScrollView(scrollView:)")));
@end

@interface ComposeAppEvent (Extensions)
- (NSString *)code __attribute__((swift_name("code()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("MainViewControllerKt")))
@interface ComposeAppMainViewControllerKt : ComposeAppBase
+ (UIViewController *)MainViewController __attribute__((swift_name("MainViewController()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Platform_iosKt")))
@interface ComposeAppPlatform_iosKt : ComposeAppBase
+ (id<ComposeAppPlatform>)getPlatform __attribute__((swift_name("getPlatform()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("PlatformPopupDismiss_iosKt")))
@interface ComposeAppPlatformPopupDismiss_iosKt : ComposeAppBase
+ (void)dismissPopupContext:(ComposeAppPlatformContext *)context __attribute__((swift_name("dismissPopup(context:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SurveyView_iosKt")))
@interface ComposeAppSurveyView_iosKt : ComposeAppBase
+ (NSString *)platformSurveyHtmlSurveyId:(NSString *)surveyId productId:(NSString *)productId __attribute__((swift_name("platformSurveyHtml(surveyId:productId:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("HtmlKt")))
@interface ComposeAppHtmlKt : ComposeAppBase

/**
 * Parser HTML simplificado para tags <p>, <b>, <i> sin anidamiento complejo.
 * - Convierte el string en una lista de párrafos con runs estilizados.
 * - Ignora cualquier otra etiqueta.
 * - No soporta atributos ni nested mezclado complejo (bold dentro de italic se marca bold+italic).
 */
+ (NSArray<ComposeAppHtmlParagraph *> *)parsePopupHtmlRaw:(NSString *)raw __attribute__((swift_name("parsePopupHtml(raw:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TimeKt")))
@interface ComposeAppTimeKt : ComposeAppBase
+ (int64_t)currentTimeMillis __attribute__((swift_name("currentTimeMillis()")));
@end

__attribute__((swift_name("KotlinThrowable")))
@interface ComposeAppKotlinThrowable : ComposeAppBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithCause:(ComposeAppKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(ComposeAppKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer));

/**
 * @note annotations
 *   kotlin.experimental.ExperimentalNativeApi
*/
- (ComposeAppKotlinArray<NSString *> *)getStackTrace __attribute__((swift_name("getStackTrace()")));
- (void)printStackTrace __attribute__((swift_name("printStackTrace()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) ComposeAppKotlinThrowable * _Nullable cause __attribute__((swift_name("cause")));
@property (readonly) NSString * _Nullable message __attribute__((swift_name("message")));
- (NSError *)asError __attribute__((swift_name("asError()")));
@end

__attribute__((swift_name("KotlinException")))
@interface ComposeAppKotlinException : ComposeAppKotlinThrowable
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithCause:(ComposeAppKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(ComposeAppKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer));
@end

__attribute__((swift_name("KotlinRuntimeException")))
@interface ComposeAppKotlinRuntimeException : ComposeAppKotlinException
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithCause:(ComposeAppKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(ComposeAppKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer));
@end

__attribute__((swift_name("KotlinIllegalStateException")))
@interface ComposeAppKotlinIllegalStateException : ComposeAppKotlinRuntimeException
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithCause:(ComposeAppKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(ComposeAppKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer));
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.4")
*/
__attribute__((swift_name("KotlinCancellationException")))
@interface ComposeAppKotlinCancellationException : ComposeAppKotlinIllegalStateException
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithMessage:(NSString * _Nullable)message __attribute__((swift_name("init(message:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithCause:(ComposeAppKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(cause:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithMessage:(NSString * _Nullable)message cause:(ComposeAppKotlinThrowable * _Nullable)cause __attribute__((swift_name("init(message:cause:)"))) __attribute__((objc_designated_initializer));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinEnumCompanion")))
@interface ComposeAppKotlinEnumCompanion : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKotlinEnumCompanion *shared __attribute__((swift_name("shared")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinArray")))
@interface ComposeAppKotlinArray<T> : ComposeAppBase
+ (instancetype)arrayWithSize:(int32_t)size init:(T _Nullable (^)(ComposeAppInt *))init __attribute__((swift_name("init(size:init:)")));
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (T _Nullable)getIndex:(int32_t)index __attribute__((swift_name("get(index:)")));
- (id<ComposeAppKotlinIterator>)iterator __attribute__((swift_name("iterator()")));
- (void)setIndex:(int32_t)index value:(T _Nullable)value __attribute__((swift_name("set(index:value:)")));
@property (readonly) int32_t size __attribute__((swift_name("size")));
@end

__attribute__((swift_name("Kotlinx_coroutines_coreCoroutineScope")))
@protocol ComposeAppKotlinx_coroutines_coreCoroutineScope
@required
@property (readonly) id<ComposeAppKotlinCoroutineContext> coroutineContext __attribute__((swift_name("coroutineContext")));
@end

__attribute__((swift_name("Ktor_ioCloseable")))
@protocol ComposeAppKtor_ioCloseable
@required
- (void)close __attribute__((swift_name("close()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_client_coreHttpClient")))
@interface ComposeAppKtor_client_coreHttpClient : ComposeAppBase <ComposeAppKotlinx_coroutines_coreCoroutineScope, ComposeAppKtor_ioCloseable>
- (instancetype)initWithEngine:(id<ComposeAppKtor_client_coreHttpClientEngine>)engine userConfig:(ComposeAppKtor_client_coreHttpClientConfig<ComposeAppKtor_client_coreHttpClientEngineConfig *> *)userConfig __attribute__((swift_name("init(engine:userConfig:)"))) __attribute__((objc_designated_initializer));
- (void)close __attribute__((swift_name("close()")));
- (ComposeAppKtor_client_coreHttpClient *)configBlock:(void (^)(ComposeAppKtor_client_coreHttpClientConfig<id> *))block __attribute__((swift_name("config(block:)")));
- (BOOL)isSupportedCapability:(id<ComposeAppKtor_client_coreHttpClientEngineCapability>)capability __attribute__((swift_name("isSupported(capability:)")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) id<ComposeAppKtor_utilsAttributes> attributes __attribute__((swift_name("attributes")));
@property (readonly) id<ComposeAppKotlinCoroutineContext> coroutineContext __attribute__((swift_name("coroutineContext")));
@property (readonly) id<ComposeAppKtor_client_coreHttpClientEngine> engine __attribute__((swift_name("engine")));
@property (readonly) ComposeAppKtor_client_coreHttpClientEngineConfig *engineConfig __attribute__((swift_name("engineConfig")));
@property (readonly) ComposeAppKtor_eventsEvents *monitor __attribute__((swift_name("monitor")));
@property (readonly) ComposeAppKtor_client_coreHttpReceivePipeline *receivePipeline __attribute__((swift_name("receivePipeline")));
@property (readonly) ComposeAppKtor_client_coreHttpRequestPipeline *requestPipeline __attribute__((swift_name("requestPipeline")));
@property (readonly) ComposeAppKtor_client_coreHttpResponsePipeline *responsePipeline __attribute__((swift_name("responsePipeline")));
@property (readonly) ComposeAppKtor_client_coreHttpSendPipeline *sendPipeline __attribute__((swift_name("sendPipeline")));
@end

__attribute__((swift_name("KotlinIterator")))
@protocol ComposeAppKotlinIterator
@required
- (BOOL)hasNext __attribute__((swift_name("hasNext()")));
- (id _Nullable)next __attribute__((swift_name("next()")));
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.3")
*/
__attribute__((swift_name("KotlinCoroutineContext")))
@protocol ComposeAppKotlinCoroutineContext
@required
- (id _Nullable)foldInitial:(id _Nullable)initial operation:(id _Nullable (^)(id _Nullable, id<ComposeAppKotlinCoroutineContextElement>))operation __attribute__((swift_name("fold(initial:operation:)")));
- (id<ComposeAppKotlinCoroutineContextElement> _Nullable)getKey:(id<ComposeAppKotlinCoroutineContextKey>)key __attribute__((swift_name("get(key:)")));
- (id<ComposeAppKotlinCoroutineContext>)minusKeyKey:(id<ComposeAppKotlinCoroutineContextKey>)key __attribute__((swift_name("minusKey(key:)")));
- (id<ComposeAppKotlinCoroutineContext>)plusContext:(id<ComposeAppKotlinCoroutineContext>)context __attribute__((swift_name("plus(context:)")));
@end

__attribute__((swift_name("Ktor_client_coreHttpClientEngine")))
@protocol ComposeAppKtor_client_coreHttpClientEngine <ComposeAppKotlinx_coroutines_coreCoroutineScope, ComposeAppKtor_ioCloseable>
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)executeData:(ComposeAppKtor_client_coreHttpRequestData *)data completionHandler:(void (^)(ComposeAppKtor_client_coreHttpResponseData * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("execute(data:completionHandler:)")));
- (void)installClient:(ComposeAppKtor_client_coreHttpClient *)client __attribute__((swift_name("install(client:)")));
@property (readonly) ComposeAppKtor_client_coreHttpClientEngineConfig *config __attribute__((swift_name("config")));
@property (readonly) ComposeAppKotlinx_coroutines_coreCoroutineDispatcher *dispatcher __attribute__((swift_name("dispatcher")));
@property (readonly) NSSet<id<ComposeAppKtor_client_coreHttpClientEngineCapability>> *supportedCapabilities __attribute__((swift_name("supportedCapabilities")));
@end

__attribute__((swift_name("Ktor_client_coreHttpClientEngineConfig")))
@interface ComposeAppKtor_client_coreHttpClientEngineConfig : ComposeAppBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
@property BOOL pipelining __attribute__((swift_name("pipelining")));
@property ComposeAppKtor_client_coreProxyConfig * _Nullable proxy __attribute__((swift_name("proxy")));
@property int32_t threadsCount __attribute__((swift_name("threadsCount"))) __attribute__((deprecated("The [threadsCount] property is deprecated. The [Dispatchers.IO] is used by default.")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_client_coreHttpClientConfig")))
@interface ComposeAppKtor_client_coreHttpClientConfig<T> : ComposeAppBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (ComposeAppKtor_client_coreHttpClientConfig<T> *)clone __attribute__((swift_name("clone()")));
- (void)engineBlock:(void (^)(T))block __attribute__((swift_name("engine(block:)")));
- (void)installClient:(ComposeAppKtor_client_coreHttpClient *)client __attribute__((swift_name("install(client:)")));
- (void)installPlugin:(id<ComposeAppKtor_client_coreHttpClientPlugin>)plugin configure:(void (^)(id))configure __attribute__((swift_name("install(plugin:configure:)")));
- (void)installKey:(NSString *)key block:(void (^)(ComposeAppKtor_client_coreHttpClient *))block __attribute__((swift_name("install(key:block:)")));
- (void)plusAssignOther:(ComposeAppKtor_client_coreHttpClientConfig<T> *)other __attribute__((swift_name("plusAssign(other:)")));
@property BOOL developmentMode __attribute__((swift_name("developmentMode")));
@property BOOL expectSuccess __attribute__((swift_name("expectSuccess")));
@property BOOL followRedirects __attribute__((swift_name("followRedirects")));
@property BOOL useDefaultTransformers __attribute__((swift_name("useDefaultTransformers")));
@end

__attribute__((swift_name("Ktor_client_coreHttpClientEngineCapability")))
@protocol ComposeAppKtor_client_coreHttpClientEngineCapability
@required
@end

__attribute__((swift_name("Ktor_utilsAttributes")))
@protocol ComposeAppKtor_utilsAttributes
@required
- (id)computeIfAbsentKey:(ComposeAppKtor_utilsAttributeKey<id> *)key block:(id (^)(void))block __attribute__((swift_name("computeIfAbsent(key:block:)")));
- (BOOL)containsKey:(ComposeAppKtor_utilsAttributeKey<id> *)key __attribute__((swift_name("contains(key:)")));
- (id)getKey_:(ComposeAppKtor_utilsAttributeKey<id> *)key __attribute__((swift_name("get(key_:)")));
- (id _Nullable)getOrNullKey:(ComposeAppKtor_utilsAttributeKey<id> *)key __attribute__((swift_name("getOrNull(key:)")));
- (void)putKey:(ComposeAppKtor_utilsAttributeKey<id> *)key value:(id)value __attribute__((swift_name("put(key:value:)")));
- (void)removeKey_:(ComposeAppKtor_utilsAttributeKey<id> *)key __attribute__((swift_name("remove(key_:)")));
- (id)takeKey:(ComposeAppKtor_utilsAttributeKey<id> *)key __attribute__((swift_name("take(key:)")));
- (id _Nullable)takeOrNullKey:(ComposeAppKtor_utilsAttributeKey<id> *)key __attribute__((swift_name("takeOrNull(key:)")));
@property (readonly) NSArray<ComposeAppKtor_utilsAttributeKey<id> *> *allKeys __attribute__((swift_name("allKeys")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_eventsEvents")))
@interface ComposeAppKtor_eventsEvents : ComposeAppBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)raiseDefinition:(ComposeAppKtor_eventsEventDefinition<id> *)definition value:(id _Nullable)value __attribute__((swift_name("raise(definition:value:)")));
- (id<ComposeAppKotlinx_coroutines_coreDisposableHandle>)subscribeDefinition:(ComposeAppKtor_eventsEventDefinition<id> *)definition handler:(void (^)(id _Nullable))handler __attribute__((swift_name("subscribe(definition:handler:)")));
- (void)unsubscribeDefinition:(ComposeAppKtor_eventsEventDefinition<id> *)definition handler:(void (^)(id _Nullable))handler __attribute__((swift_name("unsubscribe(definition:handler:)")));
@end

__attribute__((swift_name("Ktor_utilsPipeline")))
@interface ComposeAppKtor_utilsPipeline<TSubject, TContext> : ComposeAppBase
- (instancetype)initWithPhases:(ComposeAppKotlinArray<ComposeAppKtor_utilsPipelinePhase *> *)phases __attribute__((swift_name("init(phases:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithPhase:(ComposeAppKtor_utilsPipelinePhase *)phase interceptors:(NSArray<id<ComposeAppKotlinSuspendFunction2>> *)interceptors __attribute__((swift_name("init(phase:interceptors:)"))) __attribute__((objc_designated_initializer));
- (void)addPhasePhase:(ComposeAppKtor_utilsPipelinePhase *)phase __attribute__((swift_name("addPhase(phase:)")));
- (void)afterIntercepted __attribute__((swift_name("afterIntercepted()")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)executeContext:(TContext)context subject:(TSubject)subject completionHandler:(void (^)(TSubject _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("execute(context:subject:completionHandler:)")));
- (void)insertPhaseAfterReference:(ComposeAppKtor_utilsPipelinePhase *)reference phase:(ComposeAppKtor_utilsPipelinePhase *)phase __attribute__((swift_name("insertPhaseAfter(reference:phase:)")));
- (void)insertPhaseBeforeReference:(ComposeAppKtor_utilsPipelinePhase *)reference phase:(ComposeAppKtor_utilsPipelinePhase *)phase __attribute__((swift_name("insertPhaseBefore(reference:phase:)")));
- (void)interceptPhase:(ComposeAppKtor_utilsPipelinePhase *)phase block:(id<ComposeAppKotlinSuspendFunction2>)block __attribute__((swift_name("intercept(phase:block:)")));
- (NSArray<id<ComposeAppKotlinSuspendFunction2>> *)interceptorsForPhasePhase:(ComposeAppKtor_utilsPipelinePhase *)phase __attribute__((swift_name("interceptorsForPhase(phase:)")));
- (void)mergeFrom:(ComposeAppKtor_utilsPipeline<TSubject, TContext> *)from __attribute__((swift_name("merge(from:)")));
- (void)mergePhasesFrom:(ComposeAppKtor_utilsPipeline<TSubject, TContext> *)from __attribute__((swift_name("mergePhases(from:)")));
- (void)resetFromFrom:(ComposeAppKtor_utilsPipeline<TSubject, TContext> *)from __attribute__((swift_name("resetFrom(from:)")));
@property (readonly) id<ComposeAppKtor_utilsAttributes> attributes __attribute__((swift_name("attributes")));
@property (readonly) BOOL developmentMode __attribute__((swift_name("developmentMode")));
@property (readonly) BOOL isEmpty __attribute__((swift_name("isEmpty")));
@property (readonly) NSArray<ComposeAppKtor_utilsPipelinePhase *> *items __attribute__((swift_name("items")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_client_coreHttpReceivePipeline")))
@interface ComposeAppKtor_client_coreHttpReceivePipeline : ComposeAppKtor_utilsPipeline<ComposeAppKtor_client_coreHttpResponse *, ComposeAppKotlinUnit *>
- (instancetype)initWithDevelopmentMode:(BOOL)developmentMode __attribute__((swift_name("init(developmentMode:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithPhases:(ComposeAppKotlinArray<ComposeAppKtor_utilsPipelinePhase *> *)phases __attribute__((swift_name("init(phases:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
- (instancetype)initWithPhase:(ComposeAppKtor_utilsPipelinePhase *)phase interceptors:(NSArray<id<ComposeAppKotlinSuspendFunction2>> *)interceptors __attribute__((swift_name("init(phase:interceptors:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly, getter=companion) ComposeAppKtor_client_coreHttpReceivePipelinePhases *companion __attribute__((swift_name("companion")));
@property (readonly) BOOL developmentMode __attribute__((swift_name("developmentMode")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_client_coreHttpRequestPipeline")))
@interface ComposeAppKtor_client_coreHttpRequestPipeline : ComposeAppKtor_utilsPipeline<id, ComposeAppKtor_client_coreHttpRequestBuilder *>
- (instancetype)initWithDevelopmentMode:(BOOL)developmentMode __attribute__((swift_name("init(developmentMode:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithPhases:(ComposeAppKotlinArray<ComposeAppKtor_utilsPipelinePhase *> *)phases __attribute__((swift_name("init(phases:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
- (instancetype)initWithPhase:(ComposeAppKtor_utilsPipelinePhase *)phase interceptors:(NSArray<id<ComposeAppKotlinSuspendFunction2>> *)interceptors __attribute__((swift_name("init(phase:interceptors:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly, getter=companion) ComposeAppKtor_client_coreHttpRequestPipelinePhases *companion __attribute__((swift_name("companion")));
@property (readonly) BOOL developmentMode __attribute__((swift_name("developmentMode")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_client_coreHttpResponsePipeline")))
@interface ComposeAppKtor_client_coreHttpResponsePipeline : ComposeAppKtor_utilsPipeline<ComposeAppKtor_client_coreHttpResponseContainer *, ComposeAppKtor_client_coreHttpClientCall *>
- (instancetype)initWithDevelopmentMode:(BOOL)developmentMode __attribute__((swift_name("init(developmentMode:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithPhases:(ComposeAppKotlinArray<ComposeAppKtor_utilsPipelinePhase *> *)phases __attribute__((swift_name("init(phases:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
- (instancetype)initWithPhase:(ComposeAppKtor_utilsPipelinePhase *)phase interceptors:(NSArray<id<ComposeAppKotlinSuspendFunction2>> *)interceptors __attribute__((swift_name("init(phase:interceptors:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly, getter=companion) ComposeAppKtor_client_coreHttpResponsePipelinePhases *companion __attribute__((swift_name("companion")));
@property (readonly) BOOL developmentMode __attribute__((swift_name("developmentMode")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_client_coreHttpSendPipeline")))
@interface ComposeAppKtor_client_coreHttpSendPipeline : ComposeAppKtor_utilsPipeline<id, ComposeAppKtor_client_coreHttpRequestBuilder *>
- (instancetype)initWithDevelopmentMode:(BOOL)developmentMode __attribute__((swift_name("init(developmentMode:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithPhases:(ComposeAppKotlinArray<ComposeAppKtor_utilsPipelinePhase *> *)phases __attribute__((swift_name("init(phases:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
- (instancetype)initWithPhase:(ComposeAppKtor_utilsPipelinePhase *)phase interceptors:(NSArray<id<ComposeAppKotlinSuspendFunction2>> *)interceptors __attribute__((swift_name("init(phase:interceptors:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly, getter=companion) ComposeAppKtor_client_coreHttpSendPipelinePhases *companion __attribute__((swift_name("companion")));
@property (readonly) BOOL developmentMode __attribute__((swift_name("developmentMode")));
@end

__attribute__((swift_name("KotlinCoroutineContextElement")))
@protocol ComposeAppKotlinCoroutineContextElement <ComposeAppKotlinCoroutineContext>
@required
@property (readonly) id<ComposeAppKotlinCoroutineContextKey> key __attribute__((swift_name("key")));
@end

__attribute__((swift_name("KotlinCoroutineContextKey")))
@protocol ComposeAppKotlinCoroutineContextKey
@required
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_client_coreHttpRequestData")))
@interface ComposeAppKtor_client_coreHttpRequestData : ComposeAppBase
- (instancetype)initWithUrl:(ComposeAppKtor_httpUrl *)url method:(ComposeAppKtor_httpHttpMethod *)method headers:(id<ComposeAppKtor_httpHeaders>)headers body:(ComposeAppKtor_httpOutgoingContent *)body executionContext:(id<ComposeAppKotlinx_coroutines_coreJob>)executionContext attributes:(id<ComposeAppKtor_utilsAttributes>)attributes __attribute__((swift_name("init(url:method:headers:body:executionContext:attributes:)"))) __attribute__((objc_designated_initializer));
- (id _Nullable)getCapabilityOrNullKey:(id<ComposeAppKtor_client_coreHttpClientEngineCapability>)key __attribute__((swift_name("getCapabilityOrNull(key:)")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) id<ComposeAppKtor_utilsAttributes> attributes __attribute__((swift_name("attributes")));
@property (readonly) ComposeAppKtor_httpOutgoingContent *body __attribute__((swift_name("body")));
@property (readonly) id<ComposeAppKotlinx_coroutines_coreJob> executionContext __attribute__((swift_name("executionContext")));
@property (readonly) id<ComposeAppKtor_httpHeaders> headers __attribute__((swift_name("headers")));
@property (readonly) ComposeAppKtor_httpHttpMethod *method __attribute__((swift_name("method")));
@property (readonly) ComposeAppKtor_httpUrl *url __attribute__((swift_name("url")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_client_coreHttpResponseData")))
@interface ComposeAppKtor_client_coreHttpResponseData : ComposeAppBase
- (instancetype)initWithStatusCode:(ComposeAppKtor_httpHttpStatusCode *)statusCode requestTime:(ComposeAppKtor_utilsGMTDate *)requestTime headers:(id<ComposeAppKtor_httpHeaders>)headers version:(ComposeAppKtor_httpHttpProtocolVersion *)version body:(id)body callContext:(id<ComposeAppKotlinCoroutineContext>)callContext __attribute__((swift_name("init(statusCode:requestTime:headers:version:body:callContext:)"))) __attribute__((objc_designated_initializer));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) id body __attribute__((swift_name("body")));
@property (readonly) id<ComposeAppKotlinCoroutineContext> callContext __attribute__((swift_name("callContext")));
@property (readonly) id<ComposeAppKtor_httpHeaders> headers __attribute__((swift_name("headers")));
@property (readonly) ComposeAppKtor_utilsGMTDate *requestTime __attribute__((swift_name("requestTime")));
@property (readonly) ComposeAppKtor_utilsGMTDate *responseTime __attribute__((swift_name("responseTime")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *statusCode __attribute__((swift_name("statusCode")));
@property (readonly) ComposeAppKtor_httpHttpProtocolVersion *version __attribute__((swift_name("version")));
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.3")
*/
__attribute__((swift_name("KotlinAbstractCoroutineContextElement")))
@interface ComposeAppKotlinAbstractCoroutineContextElement : ComposeAppBase <ComposeAppKotlinCoroutineContextElement>
- (instancetype)initWithKey:(id<ComposeAppKotlinCoroutineContextKey>)key __attribute__((swift_name("init(key:)"))) __attribute__((objc_designated_initializer));
@property (readonly) id<ComposeAppKotlinCoroutineContextKey> key __attribute__((swift_name("key")));
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.3")
*/
__attribute__((swift_name("KotlinContinuationInterceptor")))
@protocol ComposeAppKotlinContinuationInterceptor <ComposeAppKotlinCoroutineContextElement>
@required
- (id<ComposeAppKotlinContinuation>)interceptContinuationContinuation:(id<ComposeAppKotlinContinuation>)continuation __attribute__((swift_name("interceptContinuation(continuation:)")));
- (void)releaseInterceptedContinuationContinuation:(id<ComposeAppKotlinContinuation>)continuation __attribute__((swift_name("releaseInterceptedContinuation(continuation:)")));
@end

__attribute__((swift_name("Kotlinx_coroutines_coreCoroutineDispatcher")))
@interface ComposeAppKotlinx_coroutines_coreCoroutineDispatcher : ComposeAppKotlinAbstractCoroutineContextElement <ComposeAppKotlinContinuationInterceptor>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (instancetype)initWithKey:(id<ComposeAppKotlinCoroutineContextKey>)key __attribute__((swift_name("init(key:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly, getter=companion) ComposeAppKotlinx_coroutines_coreCoroutineDispatcherKey *companion __attribute__((swift_name("companion")));
- (void)dispatchContext:(id<ComposeAppKotlinCoroutineContext>)context block:(id<ComposeAppKotlinx_coroutines_coreRunnable>)block __attribute__((swift_name("dispatch(context:block:)")));
- (void)dispatchYieldContext:(id<ComposeAppKotlinCoroutineContext>)context block:(id<ComposeAppKotlinx_coroutines_coreRunnable>)block __attribute__((swift_name("dispatchYield(context:block:)")));
- (id<ComposeAppKotlinContinuation>)interceptContinuationContinuation:(id<ComposeAppKotlinContinuation>)continuation __attribute__((swift_name("interceptContinuation(continuation:)")));
- (BOOL)isDispatchNeededContext:(id<ComposeAppKotlinCoroutineContext>)context __attribute__((swift_name("isDispatchNeeded(context:)")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
- (ComposeAppKotlinx_coroutines_coreCoroutineDispatcher *)limitedParallelismParallelism:(int32_t)parallelism __attribute__((swift_name("limitedParallelism(parallelism:)")));
- (ComposeAppKotlinx_coroutines_coreCoroutineDispatcher *)plusOther:(ComposeAppKotlinx_coroutines_coreCoroutineDispatcher *)other __attribute__((swift_name("plus(other:)"))) __attribute__((unavailable("Operator '+' on two CoroutineDispatcher objects is meaningless. CoroutineDispatcher is a coroutine context element and `+` is a set-sum operator for coroutine contexts. The dispatcher to the right of `+` just replaces the dispatcher to the left.")));
- (void)releaseInterceptedContinuationContinuation:(id<ComposeAppKotlinContinuation>)continuation __attribute__((swift_name("releaseInterceptedContinuation(continuation:)")));
- (NSString *)description __attribute__((swift_name("description()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_client_coreProxyConfig")))
@interface ComposeAppKtor_client_coreProxyConfig : ComposeAppBase
- (instancetype)initWithUrl:(ComposeAppKtor_httpUrl *)url __attribute__((swift_name("init(url:)"))) __attribute__((objc_designated_initializer));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) ComposeAppKtor_httpUrl *url __attribute__((swift_name("url")));
@end

__attribute__((swift_name("Ktor_client_coreHttpClientPlugin")))
@protocol ComposeAppKtor_client_coreHttpClientPlugin
@required
- (void)installPlugin:(id)plugin scope:(ComposeAppKtor_client_coreHttpClient *)scope __attribute__((swift_name("install(plugin:scope:)")));
- (id)prepareBlock:(void (^)(id))block __attribute__((swift_name("prepare(block:)")));
@property (readonly) ComposeAppKtor_utilsAttributeKey<id> *key __attribute__((swift_name("key")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_utilsAttributeKey")))
@interface ComposeAppKtor_utilsAttributeKey<T> : ComposeAppBase
- (instancetype)initWithName:(NSString *)name __attribute__((swift_name("init(name:)"))) __attribute__((objc_designated_initializer));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@end

__attribute__((swift_name("Ktor_eventsEventDefinition")))
@interface ComposeAppKtor_eventsEventDefinition<T> : ComposeAppBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
@end

__attribute__((swift_name("Kotlinx_coroutines_coreDisposableHandle")))
@protocol ComposeAppKotlinx_coroutines_coreDisposableHandle
@required
- (void)dispose __attribute__((swift_name("dispose()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_utilsPipelinePhase")))
@interface ComposeAppKtor_utilsPipelinePhase : ComposeAppBase
- (instancetype)initWithName:(NSString *)name __attribute__((swift_name("init(name:)"))) __attribute__((objc_designated_initializer));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@end

__attribute__((swift_name("KotlinFunction")))
@protocol ComposeAppKotlinFunction
@required
@end

__attribute__((swift_name("KotlinSuspendFunction2")))
@protocol ComposeAppKotlinSuspendFunction2 <ComposeAppKotlinFunction>
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)invokeP1:(id _Nullable)p1 p2:(id _Nullable)p2 completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("invoke(p1:p2:completionHandler:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_client_coreHttpReceivePipeline.Phases")))
@interface ComposeAppKtor_client_coreHttpReceivePipelinePhases : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)phases __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKtor_client_coreHttpReceivePipelinePhases *shared __attribute__((swift_name("shared")));
@property (readonly) ComposeAppKtor_utilsPipelinePhase *After __attribute__((swift_name("After")));
@property (readonly) ComposeAppKtor_utilsPipelinePhase *Before __attribute__((swift_name("Before")));
@property (readonly) ComposeAppKtor_utilsPipelinePhase *State __attribute__((swift_name("State")));
@end

__attribute__((swift_name("Ktor_httpHttpMessage")))
@protocol ComposeAppKtor_httpHttpMessage
@required
@property (readonly) id<ComposeAppKtor_httpHeaders> headers __attribute__((swift_name("headers")));
@end

__attribute__((swift_name("Ktor_client_coreHttpResponse")))
@interface ComposeAppKtor_client_coreHttpResponse : ComposeAppBase <ComposeAppKtor_httpHttpMessage, ComposeAppKotlinx_coroutines_coreCoroutineScope>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) ComposeAppKtor_client_coreHttpClientCall *call __attribute__((swift_name("call")));
@property (readonly) id<ComposeAppKtor_ioByteReadChannel> content __attribute__((swift_name("content")));
@property (readonly) ComposeAppKtor_utilsGMTDate *requestTime __attribute__((swift_name("requestTime")));
@property (readonly) ComposeAppKtor_utilsGMTDate *responseTime __attribute__((swift_name("responseTime")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *status __attribute__((swift_name("status")));
@property (readonly) ComposeAppKtor_httpHttpProtocolVersion *version __attribute__((swift_name("version")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinUnit")))
@interface ComposeAppKotlinUnit : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)unit __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKotlinUnit *shared __attribute__((swift_name("shared")));
- (NSString *)description __attribute__((swift_name("description()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_client_coreHttpRequestPipeline.Phases")))
@interface ComposeAppKtor_client_coreHttpRequestPipelinePhases : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)phases __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKtor_client_coreHttpRequestPipelinePhases *shared __attribute__((swift_name("shared")));
@property (readonly) ComposeAppKtor_utilsPipelinePhase *Before __attribute__((swift_name("Before")));
@property (readonly) ComposeAppKtor_utilsPipelinePhase *Render __attribute__((swift_name("Render")));
@property (readonly) ComposeAppKtor_utilsPipelinePhase *Send __attribute__((swift_name("Send")));
@property (readonly) ComposeAppKtor_utilsPipelinePhase *State __attribute__((swift_name("State")));
@property (readonly) ComposeAppKtor_utilsPipelinePhase *Transform __attribute__((swift_name("Transform")));
@end

__attribute__((swift_name("Ktor_httpHttpMessageBuilder")))
@protocol ComposeAppKtor_httpHttpMessageBuilder
@required
@property (readonly) ComposeAppKtor_httpHeadersBuilder *headers __attribute__((swift_name("headers")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_client_coreHttpRequestBuilder")))
@interface ComposeAppKtor_client_coreHttpRequestBuilder : ComposeAppBase <ComposeAppKtor_httpHttpMessageBuilder>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
@property (class, readonly, getter=companion) ComposeAppKtor_client_coreHttpRequestBuilderCompanion *companion __attribute__((swift_name("companion")));
- (ComposeAppKtor_client_coreHttpRequestData *)build __attribute__((swift_name("build()")));
- (id _Nullable)getCapabilityOrNullKey:(id<ComposeAppKtor_client_coreHttpClientEngineCapability>)key __attribute__((swift_name("getCapabilityOrNull(key:)")));
- (void)setAttributesBlock:(void (^)(id<ComposeAppKtor_utilsAttributes>))block __attribute__((swift_name("setAttributes(block:)")));
- (void)setCapabilityKey:(id<ComposeAppKtor_client_coreHttpClientEngineCapability>)key capability:(id)capability __attribute__((swift_name("setCapability(key:capability:)")));
- (ComposeAppKtor_client_coreHttpRequestBuilder *)takeFromBuilder:(ComposeAppKtor_client_coreHttpRequestBuilder *)builder __attribute__((swift_name("takeFrom(builder:)")));
- (ComposeAppKtor_client_coreHttpRequestBuilder *)takeFromWithExecutionContextBuilder:(ComposeAppKtor_client_coreHttpRequestBuilder *)builder __attribute__((swift_name("takeFromWithExecutionContext(builder:)")));
- (void)urlBlock:(void (^)(ComposeAppKtor_httpURLBuilder *, ComposeAppKtor_httpURLBuilder *))block __attribute__((swift_name("url(block:)")));
@property (readonly) id<ComposeAppKtor_utilsAttributes> attributes __attribute__((swift_name("attributes")));
@property id body __attribute__((swift_name("body")));
@property ComposeAppKtor_utilsTypeInfo * _Nullable bodyType __attribute__((swift_name("bodyType")));
@property (readonly) id<ComposeAppKotlinx_coroutines_coreJob> executionContext __attribute__((swift_name("executionContext")));
@property (readonly) ComposeAppKtor_httpHeadersBuilder *headers __attribute__((swift_name("headers")));
@property ComposeAppKtor_httpHttpMethod *method __attribute__((swift_name("method")));
@property (readonly) ComposeAppKtor_httpURLBuilder *url __attribute__((swift_name("url")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_client_coreHttpResponsePipeline.Phases")))
@interface ComposeAppKtor_client_coreHttpResponsePipelinePhases : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)phases __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKtor_client_coreHttpResponsePipelinePhases *shared __attribute__((swift_name("shared")));
@property (readonly) ComposeAppKtor_utilsPipelinePhase *After __attribute__((swift_name("After")));
@property (readonly) ComposeAppKtor_utilsPipelinePhase *Parse __attribute__((swift_name("Parse")));
@property (readonly) ComposeAppKtor_utilsPipelinePhase *Receive __attribute__((swift_name("Receive")));
@property (readonly) ComposeAppKtor_utilsPipelinePhase *State __attribute__((swift_name("State")));
@property (readonly) ComposeAppKtor_utilsPipelinePhase *Transform __attribute__((swift_name("Transform")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_client_coreHttpResponseContainer")))
@interface ComposeAppKtor_client_coreHttpResponseContainer : ComposeAppBase
- (instancetype)initWithExpectedType:(ComposeAppKtor_utilsTypeInfo *)expectedType response:(id)response __attribute__((swift_name("init(expectedType:response:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppKtor_client_coreHttpResponseContainer *)doCopyExpectedType:(ComposeAppKtor_utilsTypeInfo *)expectedType response:(id)response __attribute__((swift_name("doCopy(expectedType:response:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) ComposeAppKtor_utilsTypeInfo *expectedType __attribute__((swift_name("expectedType")));
@property (readonly) id response __attribute__((swift_name("response")));
@end

__attribute__((swift_name("Ktor_client_coreHttpClientCall")))
@interface ComposeAppKtor_client_coreHttpClientCall : ComposeAppBase <ComposeAppKotlinx_coroutines_coreCoroutineScope>
- (instancetype)initWithClient:(ComposeAppKtor_client_coreHttpClient *)client __attribute__((swift_name("init(client:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithClient:(ComposeAppKtor_client_coreHttpClient *)client requestData:(ComposeAppKtor_client_coreHttpRequestData *)requestData responseData:(ComposeAppKtor_client_coreHttpResponseData *)responseData __attribute__((swift_name("init(client:requestData:responseData:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) ComposeAppKtor_client_coreHttpClientCallCompanion *companion __attribute__((swift_name("companion")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)bodyInfo:(ComposeAppKtor_utilsTypeInfo *)info completionHandler:(void (^)(id _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("body(info:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)bodyNullableInfo:(ComposeAppKtor_utilsTypeInfo *)info completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("bodyNullable(info:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)getResponseContentWithCompletionHandler:(void (^)(id<ComposeAppKtor_ioByteReadChannel> _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("getResponseContent(completionHandler:)")));
- (NSString *)description __attribute__((swift_name("description()")));

/**
 * @note This property has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
@property (readonly) BOOL allowDoubleReceive __attribute__((swift_name("allowDoubleReceive")));
@property (readonly) id<ComposeAppKtor_utilsAttributes> attributes __attribute__((swift_name("attributes")));
@property (readonly) ComposeAppKtor_client_coreHttpClient *client __attribute__((swift_name("client")));
@property (readonly) id<ComposeAppKotlinCoroutineContext> coroutineContext __attribute__((swift_name("coroutineContext")));
@property id<ComposeAppKtor_client_coreHttpRequest> request __attribute__((swift_name("request")));
@property ComposeAppKtor_client_coreHttpResponse *response __attribute__((swift_name("response")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_client_coreHttpSendPipeline.Phases")))
@interface ComposeAppKtor_client_coreHttpSendPipelinePhases : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)phases __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKtor_client_coreHttpSendPipelinePhases *shared __attribute__((swift_name("shared")));
@property (readonly) ComposeAppKtor_utilsPipelinePhase *Before __attribute__((swift_name("Before")));
@property (readonly) ComposeAppKtor_utilsPipelinePhase *Engine __attribute__((swift_name("Engine")));
@property (readonly) ComposeAppKtor_utilsPipelinePhase *Monitoring __attribute__((swift_name("Monitoring")));
@property (readonly) ComposeAppKtor_utilsPipelinePhase *Receive __attribute__((swift_name("Receive")));
@property (readonly) ComposeAppKtor_utilsPipelinePhase *State __attribute__((swift_name("State")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_httpUrl")))
@interface ComposeAppKtor_httpUrl : ComposeAppBase
@property (class, readonly, getter=companion) ComposeAppKtor_httpUrlCompanion *companion __attribute__((swift_name("companion")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *encodedFragment __attribute__((swift_name("encodedFragment")));
@property (readonly) NSString * _Nullable encodedPassword __attribute__((swift_name("encodedPassword")));
@property (readonly) NSString *encodedPath __attribute__((swift_name("encodedPath")));
@property (readonly) NSString *encodedPathAndQuery __attribute__((swift_name("encodedPathAndQuery")));
@property (readonly) NSString *encodedQuery __attribute__((swift_name("encodedQuery")));
@property (readonly) NSString * _Nullable encodedUser __attribute__((swift_name("encodedUser")));
@property (readonly) NSString *fragment __attribute__((swift_name("fragment")));
@property (readonly) NSString *host __attribute__((swift_name("host")));
@property (readonly) id<ComposeAppKtor_httpParameters> parameters __attribute__((swift_name("parameters")));
@property (readonly) NSString * _Nullable password __attribute__((swift_name("password")));
@property (readonly) NSArray<NSString *> *pathSegments __attribute__((swift_name("pathSegments")));
@property (readonly) int32_t port __attribute__((swift_name("port")));
@property (readonly) ComposeAppKtor_httpURLProtocol *protocol __attribute__((swift_name("protocol")));
@property (readonly) int32_t specifiedPort __attribute__((swift_name("specifiedPort")));
@property (readonly) BOOL trailingQuery __attribute__((swift_name("trailingQuery")));
@property (readonly) NSString * _Nullable user __attribute__((swift_name("user")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_httpHttpMethod")))
@interface ComposeAppKtor_httpHttpMethod : ComposeAppBase
- (instancetype)initWithValue:(NSString *)value __attribute__((swift_name("init(value:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) ComposeAppKtor_httpHttpMethodCompanion *companion __attribute__((swift_name("companion")));
- (ComposeAppKtor_httpHttpMethod *)doCopyValue:(NSString *)value __attribute__((swift_name("doCopy(value:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *value __attribute__((swift_name("value")));
@end

__attribute__((swift_name("Ktor_utilsStringValues")))
@protocol ComposeAppKtor_utilsStringValues
@required
- (BOOL)containsName:(NSString *)name __attribute__((swift_name("contains(name:)")));
- (BOOL)containsName:(NSString *)name value:(NSString *)value __attribute__((swift_name("contains(name:value:)")));
- (NSSet<id<ComposeAppKotlinMapEntry>> *)entries __attribute__((swift_name("entries()")));
- (void)forEachBody:(void (^)(NSString *, NSArray<NSString *> *))body __attribute__((swift_name("forEach(body:)")));
- (NSString * _Nullable)getName:(NSString *)name __attribute__((swift_name("get(name:)")));
- (NSArray<NSString *> * _Nullable)getAllName:(NSString *)name __attribute__((swift_name("getAll(name:)")));
- (BOOL)isEmpty_ __attribute__((swift_name("isEmpty()")));
- (NSSet<NSString *> *)names __attribute__((swift_name("names()")));
@property (readonly) BOOL caseInsensitiveName __attribute__((swift_name("caseInsensitiveName")));
@end

__attribute__((swift_name("Ktor_httpHeaders")))
@protocol ComposeAppKtor_httpHeaders <ComposeAppKtor_utilsStringValues>
@required
@end

__attribute__((swift_name("Ktor_httpOutgoingContent")))
@interface ComposeAppKtor_httpOutgoingContent : ComposeAppBase
- (id _Nullable)getPropertyKey:(ComposeAppKtor_utilsAttributeKey<id> *)key __attribute__((swift_name("getProperty(key:)")));
- (void)setPropertyKey:(ComposeAppKtor_utilsAttributeKey<id> *)key value:(id _Nullable)value __attribute__((swift_name("setProperty(key:value:)")));
- (id<ComposeAppKtor_httpHeaders> _Nullable)trailers __attribute__((swift_name("trailers()")));
@property (readonly) ComposeAppLong * _Nullable contentLength __attribute__((swift_name("contentLength")));
@property (readonly) ComposeAppKtor_httpContentType * _Nullable contentType __attribute__((swift_name("contentType")));
@property (readonly) id<ComposeAppKtor_httpHeaders> headers __attribute__((swift_name("headers")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode * _Nullable status __attribute__((swift_name("status")));
@end

__attribute__((swift_name("Kotlinx_coroutines_coreJob")))
@protocol ComposeAppKotlinx_coroutines_coreJob <ComposeAppKotlinCoroutineContextElement>
@required
- (id<ComposeAppKotlinx_coroutines_coreChildHandle>)attachChildChild:(id<ComposeAppKotlinx_coroutines_coreChildJob>)child __attribute__((swift_name("attachChild(child:)")));
- (void)cancelCause:(ComposeAppKotlinCancellationException * _Nullable)cause __attribute__((swift_name("cancel(cause:)")));
- (ComposeAppKotlinCancellationException *)getCancellationException __attribute__((swift_name("getCancellationException()")));
- (id<ComposeAppKotlinx_coroutines_coreDisposableHandle>)invokeOnCompletionHandler:(void (^)(ComposeAppKotlinThrowable * _Nullable))handler __attribute__((swift_name("invokeOnCompletion(handler:)")));
- (id<ComposeAppKotlinx_coroutines_coreDisposableHandle>)invokeOnCompletionOnCancelling:(BOOL)onCancelling invokeImmediately:(BOOL)invokeImmediately handler:(void (^)(ComposeAppKotlinThrowable * _Nullable))handler __attribute__((swift_name("invokeOnCompletion(onCancelling:invokeImmediately:handler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)joinWithCompletionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("join(completionHandler:)")));
- (id<ComposeAppKotlinx_coroutines_coreJob>)plusOther_:(id<ComposeAppKotlinx_coroutines_coreJob>)other __attribute__((swift_name("plus(other_:)"))) __attribute__((unavailable("Operator '+' on two Job objects is meaningless. Job is a coroutine context element and `+` is a set-sum operator for coroutine contexts. The job to the right of `+` just replaces the job the left of `+`.")));
- (BOOL)start __attribute__((swift_name("start()")));
@property (readonly) id<ComposeAppKotlinSequence> children __attribute__((swift_name("children")));
@property (readonly) BOOL isActive __attribute__((swift_name("isActive")));
@property (readonly) BOOL isCancelled __attribute__((swift_name("isCancelled")));
@property (readonly) BOOL isCompleted __attribute__((swift_name("isCompleted")));
@property (readonly) id<ComposeAppKotlinx_coroutines_coreSelectClause0> onJoin __attribute__((swift_name("onJoin")));

/**
 * @note annotations
 *   kotlinx.coroutines.ExperimentalCoroutinesApi
*/
@property (readonly) id<ComposeAppKotlinx_coroutines_coreJob> _Nullable parent __attribute__((swift_name("parent")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_httpHttpStatusCode")))
@interface ComposeAppKtor_httpHttpStatusCode : ComposeAppBase <ComposeAppKotlinComparable>
- (instancetype)initWithValue:(int32_t)value description:(NSString *)description __attribute__((swift_name("init(value:description:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) ComposeAppKtor_httpHttpStatusCodeCompanion *companion __attribute__((swift_name("companion")));
- (int32_t)compareToOther:(ComposeAppKtor_httpHttpStatusCode *)other __attribute__((swift_name("compareTo(other:)")));
- (ComposeAppKtor_httpHttpStatusCode *)doCopyValue:(int32_t)value description:(NSString *)description __attribute__((swift_name("doCopy(value:description:)")));
- (ComposeAppKtor_httpHttpStatusCode *)descriptionValue:(NSString *)value __attribute__((swift_name("description(value:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *description_ __attribute__((swift_name("description_")));
@property (readonly) int32_t value __attribute__((swift_name("value")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_utilsGMTDate")))
@interface ComposeAppKtor_utilsGMTDate : ComposeAppBase <ComposeAppKotlinComparable>
@property (class, readonly, getter=companion) ComposeAppKtor_utilsGMTDateCompanion *companion __attribute__((swift_name("companion")));
- (int32_t)compareToOther:(ComposeAppKtor_utilsGMTDate *)other __attribute__((swift_name("compareTo(other:)")));
- (ComposeAppKtor_utilsGMTDate *)doCopySeconds:(int32_t)seconds minutes:(int32_t)minutes hours:(int32_t)hours dayOfWeek:(ComposeAppKtor_utilsWeekDay *)dayOfWeek dayOfMonth:(int32_t)dayOfMonth dayOfYear:(int32_t)dayOfYear month:(ComposeAppKtor_utilsMonth *)month year:(int32_t)year timestamp:(int64_t)timestamp __attribute__((swift_name("doCopy(seconds:minutes:hours:dayOfWeek:dayOfMonth:dayOfYear:month:year:timestamp:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t dayOfMonth __attribute__((swift_name("dayOfMonth")));
@property (readonly) ComposeAppKtor_utilsWeekDay *dayOfWeek __attribute__((swift_name("dayOfWeek")));
@property (readonly) int32_t dayOfYear __attribute__((swift_name("dayOfYear")));
@property (readonly) int32_t hours __attribute__((swift_name("hours")));
@property (readonly) int32_t minutes __attribute__((swift_name("minutes")));
@property (readonly) ComposeAppKtor_utilsMonth *month __attribute__((swift_name("month")));
@property (readonly) int32_t seconds __attribute__((swift_name("seconds")));
@property (readonly) int64_t timestamp __attribute__((swift_name("timestamp")));
@property (readonly) int32_t year __attribute__((swift_name("year")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_httpHttpProtocolVersion")))
@interface ComposeAppKtor_httpHttpProtocolVersion : ComposeAppBase
- (instancetype)initWithName:(NSString *)name major:(int32_t)major minor:(int32_t)minor __attribute__((swift_name("init(name:major:minor:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) ComposeAppKtor_httpHttpProtocolVersionCompanion *companion __attribute__((swift_name("companion")));
- (ComposeAppKtor_httpHttpProtocolVersion *)doCopyName:(NSString *)name major:(int32_t)major minor:(int32_t)minor __attribute__((swift_name("doCopy(name:major:minor:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t major __attribute__((swift_name("major")));
@property (readonly) int32_t minor __attribute__((swift_name("minor")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.3")
*/
__attribute__((swift_name("KotlinContinuation")))
@protocol ComposeAppKotlinContinuation
@required
- (void)resumeWithResult:(id _Nullable)result __attribute__((swift_name("resumeWith(result:)")));
@property (readonly) id<ComposeAppKotlinCoroutineContext> context __attribute__((swift_name("context")));
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.3")
 *   kotlin.ExperimentalStdlibApi
*/
__attribute__((swift_name("KotlinAbstractCoroutineContextKey")))
@interface ComposeAppKotlinAbstractCoroutineContextKey<B, E> : ComposeAppBase <ComposeAppKotlinCoroutineContextKey>
- (instancetype)initWithBaseKey:(id<ComposeAppKotlinCoroutineContextKey>)baseKey safeCast:(E _Nullable (^)(id<ComposeAppKotlinCoroutineContextElement>))safeCast __attribute__((swift_name("init(baseKey:safeCast:)"))) __attribute__((objc_designated_initializer));
@end


/**
 * @note annotations
 *   kotlin.ExperimentalStdlibApi
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Kotlinx_coroutines_coreCoroutineDispatcher.Key")))
@interface ComposeAppKotlinx_coroutines_coreCoroutineDispatcherKey : ComposeAppKotlinAbstractCoroutineContextKey<id<ComposeAppKotlinContinuationInterceptor>, ComposeAppKotlinx_coroutines_coreCoroutineDispatcher *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithBaseKey:(id<ComposeAppKotlinCoroutineContextKey>)baseKey safeCast:(id<ComposeAppKotlinCoroutineContextElement> _Nullable (^)(id<ComposeAppKotlinCoroutineContextElement>))safeCast __attribute__((swift_name("init(baseKey:safeCast:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
+ (instancetype)key __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKotlinx_coroutines_coreCoroutineDispatcherKey *shared __attribute__((swift_name("shared")));
@end

__attribute__((swift_name("Kotlinx_coroutines_coreRunnable")))
@protocol ComposeAppKotlinx_coroutines_coreRunnable
@required
- (void)run __attribute__((swift_name("run()")));
@end

__attribute__((swift_name("Ktor_ioByteReadChannel")))
@protocol ComposeAppKtor_ioByteReadChannel
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)awaitContentWithCompletionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("awaitContent(completionHandler:)")));
- (BOOL)cancelCause_:(ComposeAppKotlinThrowable * _Nullable)cause __attribute__((swift_name("cancel(cause_:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)discardMax:(int64_t)max completionHandler:(void (^)(ComposeAppLong * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("discard(max:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)peekToDestination:(ComposeAppKtor_ioMemory *)destination destinationOffset:(int64_t)destinationOffset offset:(int64_t)offset min:(int64_t)min max:(int64_t)max completionHandler:(void (^)(ComposeAppLong * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("peekTo(destination:destinationOffset:offset:min:max:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)readAvailableDst:(ComposeAppKtor_ioChunkBuffer *)dst completionHandler:(void (^)(ComposeAppInt * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("readAvailable(dst:completionHandler:)")));
- (int32_t)readAvailableMin:(int32_t)min block:(void (^)(ComposeAppKtor_ioBuffer *))block __attribute__((swift_name("readAvailable(min:block:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)readAvailableDst:(ComposeAppKotlinByteArray *)dst offset:(int32_t)offset length:(int32_t)length completionHandler:(void (^)(ComposeAppInt * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("readAvailable(dst:offset:length:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)readAvailableDst:(void *)dst offset:(int32_t)offset length:(int32_t)length completionHandler_:(void (^)(ComposeAppInt * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("readAvailable(dst:offset:length:completionHandler_:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)readAvailableDst:(void *)dst offset:(int64_t)offset length:(int64_t)length completionHandler__:(void (^)(ComposeAppInt * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("readAvailable(dst:offset:length:completionHandler__:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)readBooleanWithCompletionHandler:(void (^)(ComposeAppBoolean * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("readBoolean(completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)readByteWithCompletionHandler:(void (^)(ComposeAppByte * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("readByte(completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)readDoubleWithCompletionHandler:(void (^)(ComposeAppDouble * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("readDouble(completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)readFloatWithCompletionHandler:(void (^)(ComposeAppFloat * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("readFloat(completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)readFullyDst:(ComposeAppKtor_ioChunkBuffer *)dst n:(int32_t)n completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("readFully(dst:n:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)readFullyDst:(ComposeAppKotlinByteArray *)dst offset:(int32_t)offset length:(int32_t)length completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("readFully(dst:offset:length:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)readFullyDst:(void *)dst offset:(int32_t)offset length:(int32_t)length completionHandler_:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("readFully(dst:offset:length:completionHandler_:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)readFullyDst:(void *)dst offset:(int64_t)offset length:(int64_t)length completionHandler__:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("readFully(dst:offset:length:completionHandler__:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)readIntWithCompletionHandler:(void (^)(ComposeAppInt * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("readInt(completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)readLongWithCompletionHandler:(void (^)(ComposeAppLong * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("readLong(completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)readPacketSize:(int32_t)size completionHandler:(void (^)(ComposeAppKtor_ioByteReadPacket * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("readPacket(size:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)readRemainingLimit:(int64_t)limit completionHandler:(void (^)(ComposeAppKtor_ioByteReadPacket * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("readRemaining(limit:completionHandler:)")));
- (void)readSessionConsumer:(void (^)(id<ComposeAppKtor_ioReadSession>))consumer __attribute__((swift_name("readSession(consumer:)"))) __attribute__((deprecated("Use read { } instead.")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)readShortWithCompletionHandler:(void (^)(ComposeAppShort * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("readShort(completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)readSuspendableSessionConsumer:(id<ComposeAppKotlinSuspendFunction1>)consumer completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("readSuspendableSession(consumer:completionHandler:)"))) __attribute__((deprecated("Use read { } instead.")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)readUTF8LineLimit:(int32_t)limit completionHandler:(void (^)(NSString * _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("readUTF8Line(limit:completionHandler:)")));

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)readUTF8LineToOut:(id<ComposeAppKotlinAppendable>)out limit:(int32_t)limit completionHandler:(void (^)(ComposeAppBoolean * _Nullable, NSError * _Nullable))completionHandler __attribute__((swift_name("readUTF8LineTo(out:limit:completionHandler:)")));
@property (readonly) int32_t availableForRead __attribute__((swift_name("availableForRead")));
@property (readonly) ComposeAppKotlinThrowable * _Nullable closedCause __attribute__((swift_name("closedCause")));
@property (readonly) BOOL isClosedForRead __attribute__((swift_name("isClosedForRead")));
@property (readonly) BOOL isClosedForWrite __attribute__((swift_name("isClosedForWrite")));
@property (readonly) int64_t totalBytesRead __attribute__((swift_name("totalBytesRead")));
@end

__attribute__((swift_name("Ktor_utilsStringValuesBuilder")))
@protocol ComposeAppKtor_utilsStringValuesBuilder
@required
- (void)appendName:(NSString *)name value:(NSString *)value __attribute__((swift_name("append(name:value:)")));
- (void)appendAllStringValues:(id<ComposeAppKtor_utilsStringValues>)stringValues __attribute__((swift_name("appendAll(stringValues:)")));
- (void)appendAllName:(NSString *)name values:(id)values __attribute__((swift_name("appendAll(name:values:)")));
- (void)appendMissingStringValues:(id<ComposeAppKtor_utilsStringValues>)stringValues __attribute__((swift_name("appendMissing(stringValues:)")));
- (void)appendMissingName:(NSString *)name values:(id)values __attribute__((swift_name("appendMissing(name:values:)")));
- (id<ComposeAppKtor_utilsStringValues>)build __attribute__((swift_name("build()")));
- (void)clear __attribute__((swift_name("clear()")));
- (BOOL)containsName:(NSString *)name __attribute__((swift_name("contains(name:)")));
- (BOOL)containsName:(NSString *)name value:(NSString *)value __attribute__((swift_name("contains(name:value:)")));
- (NSSet<id<ComposeAppKotlinMapEntry>> *)entries __attribute__((swift_name("entries()")));
- (NSString * _Nullable)getName:(NSString *)name __attribute__((swift_name("get(name:)")));
- (NSArray<NSString *> * _Nullable)getAllName:(NSString *)name __attribute__((swift_name("getAll(name:)")));
- (BOOL)isEmpty_ __attribute__((swift_name("isEmpty()")));
- (NSSet<NSString *> *)names __attribute__((swift_name("names()")));
- (void)removeName:(NSString *)name __attribute__((swift_name("remove(name:)")));
- (BOOL)removeName:(NSString *)name value:(NSString *)value __attribute__((swift_name("remove(name:value:)")));
- (void)removeKeysWithNoEntries __attribute__((swift_name("removeKeysWithNoEntries()")));
- (void)setName:(NSString *)name value:(NSString *)value __attribute__((swift_name("set(name:value:)")));
@property (readonly) BOOL caseInsensitiveName __attribute__((swift_name("caseInsensitiveName")));
@end

__attribute__((swift_name("Ktor_utilsStringValuesBuilderImpl")))
@interface ComposeAppKtor_utilsStringValuesBuilderImpl : ComposeAppBase <ComposeAppKtor_utilsStringValuesBuilder>
- (instancetype)initWithCaseInsensitiveName:(BOOL)caseInsensitiveName size:(int32_t)size __attribute__((swift_name("init(caseInsensitiveName:size:)"))) __attribute__((objc_designated_initializer));
- (void)appendName:(NSString *)name value:(NSString *)value __attribute__((swift_name("append(name:value:)")));
- (void)appendAllStringValues:(id<ComposeAppKtor_utilsStringValues>)stringValues __attribute__((swift_name("appendAll(stringValues:)")));
- (void)appendAllName:(NSString *)name values:(id)values __attribute__((swift_name("appendAll(name:values:)")));
- (void)appendMissingStringValues:(id<ComposeAppKtor_utilsStringValues>)stringValues __attribute__((swift_name("appendMissing(stringValues:)")));
- (void)appendMissingName:(NSString *)name values:(id)values __attribute__((swift_name("appendMissing(name:values:)")));
- (id<ComposeAppKtor_utilsStringValues>)build __attribute__((swift_name("build()")));
- (void)clear __attribute__((swift_name("clear()")));
- (BOOL)containsName:(NSString *)name __attribute__((swift_name("contains(name:)")));
- (BOOL)containsName:(NSString *)name value:(NSString *)value __attribute__((swift_name("contains(name:value:)")));
- (NSSet<id<ComposeAppKotlinMapEntry>> *)entries __attribute__((swift_name("entries()")));
- (NSString * _Nullable)getName:(NSString *)name __attribute__((swift_name("get(name:)")));
- (NSArray<NSString *> * _Nullable)getAllName:(NSString *)name __attribute__((swift_name("getAll(name:)")));
- (BOOL)isEmpty_ __attribute__((swift_name("isEmpty()")));
- (NSSet<NSString *> *)names __attribute__((swift_name("names()")));
- (void)removeName:(NSString *)name __attribute__((swift_name("remove(name:)")));
- (BOOL)removeName:(NSString *)name value:(NSString *)value __attribute__((swift_name("remove(name:value:)")));
- (void)removeKeysWithNoEntries __attribute__((swift_name("removeKeysWithNoEntries()")));
- (void)setName:(NSString *)name value:(NSString *)value __attribute__((swift_name("set(name:value:)")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)validateNameName:(NSString *)name __attribute__((swift_name("validateName(name:)")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)validateValueValue:(NSString *)value __attribute__((swift_name("validateValue(value:)")));
@property (readonly) BOOL caseInsensitiveName __attribute__((swift_name("caseInsensitiveName")));

/**
 * @note This property has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
@property (readonly) ComposeAppMutableDictionary<NSString *, NSMutableArray<NSString *> *> *values __attribute__((swift_name("values")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_httpHeadersBuilder")))
@interface ComposeAppKtor_httpHeadersBuilder : ComposeAppKtor_utilsStringValuesBuilderImpl
- (instancetype)initWithSize:(int32_t)size __attribute__((swift_name("init(size:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithCaseInsensitiveName:(BOOL)caseInsensitiveName size:(int32_t)size __attribute__((swift_name("init(caseInsensitiveName:size:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
- (id<ComposeAppKtor_httpHeaders>)build __attribute__((swift_name("build()")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)validateNameName:(NSString *)name __attribute__((swift_name("validateName(name:)")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)validateValueValue:(NSString *)value __attribute__((swift_name("validateValue(value:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_client_coreHttpRequestBuilder.Companion")))
@interface ComposeAppKtor_client_coreHttpRequestBuilderCompanion : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKtor_client_coreHttpRequestBuilderCompanion *shared __attribute__((swift_name("shared")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_httpURLBuilder")))
@interface ComposeAppKtor_httpURLBuilder : ComposeAppBase
- (instancetype)initWithProtocol:(ComposeAppKtor_httpURLProtocol *)protocol host:(NSString *)host port:(int32_t)port user:(NSString * _Nullable)user password:(NSString * _Nullable)password pathSegments:(NSArray<NSString *> *)pathSegments parameters:(id<ComposeAppKtor_httpParameters>)parameters fragment:(NSString *)fragment trailingQuery:(BOOL)trailingQuery __attribute__((swift_name("init(protocol:host:port:user:password:pathSegments:parameters:fragment:trailingQuery:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) ComposeAppKtor_httpURLBuilderCompanion *companion __attribute__((swift_name("companion")));
- (ComposeAppKtor_httpUrl *)build __attribute__((swift_name("build()")));
- (NSString *)buildString __attribute__((swift_name("buildString()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property NSString *encodedFragment __attribute__((swift_name("encodedFragment")));
@property id<ComposeAppKtor_httpParametersBuilder> encodedParameters __attribute__((swift_name("encodedParameters")));
@property NSString * _Nullable encodedPassword __attribute__((swift_name("encodedPassword")));
@property NSArray<NSString *> *encodedPathSegments __attribute__((swift_name("encodedPathSegments")));
@property NSString * _Nullable encodedUser __attribute__((swift_name("encodedUser")));
@property NSString *fragment __attribute__((swift_name("fragment")));
@property NSString *host __attribute__((swift_name("host")));
@property (readonly) id<ComposeAppKtor_httpParametersBuilder> parameters __attribute__((swift_name("parameters")));
@property NSString * _Nullable password __attribute__((swift_name("password")));
@property NSArray<NSString *> *pathSegments __attribute__((swift_name("pathSegments")));
@property int32_t port __attribute__((swift_name("port")));
@property ComposeAppKtor_httpURLProtocol *protocol __attribute__((swift_name("protocol")));
@property BOOL trailingQuery __attribute__((swift_name("trailingQuery")));
@property NSString * _Nullable user __attribute__((swift_name("user")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_utilsTypeInfo")))
@interface ComposeAppKtor_utilsTypeInfo : ComposeAppBase
- (instancetype)initWithType:(id<ComposeAppKotlinKClass>)type reifiedType:(id<ComposeAppKotlinKType>)reifiedType kotlinType:(id<ComposeAppKotlinKType> _Nullable)kotlinType __attribute__((swift_name("init(type:reifiedType:kotlinType:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppKtor_utilsTypeInfo *)doCopyType:(id<ComposeAppKotlinKClass>)type reifiedType:(id<ComposeAppKotlinKType>)reifiedType kotlinType:(id<ComposeAppKotlinKType> _Nullable)kotlinType __attribute__((swift_name("doCopy(type:reifiedType:kotlinType:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) id<ComposeAppKotlinKType> _Nullable kotlinType __attribute__((swift_name("kotlinType")));
@property (readonly) id<ComposeAppKotlinKType> reifiedType __attribute__((swift_name("reifiedType")));
@property (readonly) id<ComposeAppKotlinKClass> type __attribute__((swift_name("type")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_client_coreHttpClientCall.Companion")))
@interface ComposeAppKtor_client_coreHttpClientCallCompanion : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKtor_client_coreHttpClientCallCompanion *shared __attribute__((swift_name("shared")));
@property (readonly) ComposeAppKtor_utilsAttributeKey<id> *CustomResponse __attribute__((swift_name("CustomResponse"))) __attribute__((unavailable("This is going to be removed. Please file a ticket with clarification why and what for do you need it.")));
@end

__attribute__((swift_name("Ktor_client_coreHttpRequest")))
@protocol ComposeAppKtor_client_coreHttpRequest <ComposeAppKtor_httpHttpMessage, ComposeAppKotlinx_coroutines_coreCoroutineScope>
@required
@property (readonly) id<ComposeAppKtor_utilsAttributes> attributes __attribute__((swift_name("attributes")));
@property (readonly) ComposeAppKtor_client_coreHttpClientCall *call __attribute__((swift_name("call")));
@property (readonly) ComposeAppKtor_httpOutgoingContent *content __attribute__((swift_name("content")));
@property (readonly) ComposeAppKtor_httpHttpMethod *method __attribute__((swift_name("method")));
@property (readonly) ComposeAppKtor_httpUrl *url __attribute__((swift_name("url")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_httpUrl.Companion")))
@interface ComposeAppKtor_httpUrlCompanion : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKtor_httpUrlCompanion *shared __attribute__((swift_name("shared")));
@end

__attribute__((swift_name("Ktor_httpParameters")))
@protocol ComposeAppKtor_httpParameters <ComposeAppKtor_utilsStringValues>
@required
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_httpURLProtocol")))
@interface ComposeAppKtor_httpURLProtocol : ComposeAppBase
- (instancetype)initWithName:(NSString *)name defaultPort:(int32_t)defaultPort __attribute__((swift_name("init(name:defaultPort:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) ComposeAppKtor_httpURLProtocolCompanion *companion __attribute__((swift_name("companion")));
- (ComposeAppKtor_httpURLProtocol *)doCopyName:(NSString *)name defaultPort:(int32_t)defaultPort __attribute__((swift_name("doCopy(name:defaultPort:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t defaultPort __attribute__((swift_name("defaultPort")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_httpHttpMethod.Companion")))
@interface ComposeAppKtor_httpHttpMethodCompanion : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKtor_httpHttpMethodCompanion *shared __attribute__((swift_name("shared")));
- (ComposeAppKtor_httpHttpMethod *)parseMethod:(NSString *)method __attribute__((swift_name("parse(method:)")));
@property (readonly) NSArray<ComposeAppKtor_httpHttpMethod *> *DefaultMethods __attribute__((swift_name("DefaultMethods")));
@property (readonly) ComposeAppKtor_httpHttpMethod *Delete __attribute__((swift_name("Delete")));
@property (readonly) ComposeAppKtor_httpHttpMethod *Get __attribute__((swift_name("Get")));
@property (readonly) ComposeAppKtor_httpHttpMethod *Head __attribute__((swift_name("Head")));
@property (readonly) ComposeAppKtor_httpHttpMethod *Options __attribute__((swift_name("Options")));
@property (readonly) ComposeAppKtor_httpHttpMethod *Patch __attribute__((swift_name("Patch")));
@property (readonly) ComposeAppKtor_httpHttpMethod *Post __attribute__((swift_name("Post")));
@property (readonly) ComposeAppKtor_httpHttpMethod *Put __attribute__((swift_name("Put")));
@end

__attribute__((swift_name("KotlinMapEntry")))
@protocol ComposeAppKotlinMapEntry
@required
@property (readonly) id _Nullable key __attribute__((swift_name("key")));
@property (readonly) id _Nullable value __attribute__((swift_name("value")));
@end

__attribute__((swift_name("Ktor_httpHeaderValueWithParameters")))
@interface ComposeAppKtor_httpHeaderValueWithParameters : ComposeAppBase
- (instancetype)initWithContent:(NSString *)content parameters:(NSArray<ComposeAppKtor_httpHeaderValueParam *> *)parameters __attribute__((swift_name("init(content:parameters:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) ComposeAppKtor_httpHeaderValueWithParametersCompanion *companion __attribute__((swift_name("companion")));
- (NSString * _Nullable)parameterName:(NSString *)name __attribute__((swift_name("parameter(name:)")));
- (NSString *)description __attribute__((swift_name("description()")));

/**
 * @note This property has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
@property (readonly) NSString *content __attribute__((swift_name("content")));
@property (readonly) NSArray<ComposeAppKtor_httpHeaderValueParam *> *parameters __attribute__((swift_name("parameters")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_httpContentType")))
@interface ComposeAppKtor_httpContentType : ComposeAppKtor_httpHeaderValueWithParameters
- (instancetype)initWithContentType:(NSString *)contentType contentSubtype:(NSString *)contentSubtype parameters:(NSArray<ComposeAppKtor_httpHeaderValueParam *> *)parameters __attribute__((swift_name("init(contentType:contentSubtype:parameters:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithContent:(NSString *)content parameters:(NSArray<ComposeAppKtor_httpHeaderValueParam *> *)parameters __attribute__((swift_name("init(content:parameters:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly, getter=companion) ComposeAppKtor_httpContentTypeCompanion *companion __attribute__((swift_name("companion")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (BOOL)matchPattern:(ComposeAppKtor_httpContentType *)pattern __attribute__((swift_name("match(pattern:)")));
- (BOOL)matchPattern_:(NSString *)pattern __attribute__((swift_name("match(pattern_:)")));
- (ComposeAppKtor_httpContentType *)withParameterName:(NSString *)name value:(NSString *)value __attribute__((swift_name("withParameter(name:value:)")));
- (ComposeAppKtor_httpContentType *)withoutParameters __attribute__((swift_name("withoutParameters()")));
@property (readonly) NSString *contentSubtype __attribute__((swift_name("contentSubtype")));
@property (readonly) NSString *contentType __attribute__((swift_name("contentType")));
@end

__attribute__((swift_name("Kotlinx_coroutines_coreChildHandle")))
@protocol ComposeAppKotlinx_coroutines_coreChildHandle <ComposeAppKotlinx_coroutines_coreDisposableHandle>
@required
- (BOOL)childCancelledCause:(ComposeAppKotlinThrowable *)cause __attribute__((swift_name("childCancelled(cause:)")));
@property (readonly) id<ComposeAppKotlinx_coroutines_coreJob> _Nullable parent __attribute__((swift_name("parent")));
@end

__attribute__((swift_name("Kotlinx_coroutines_coreChildJob")))
@protocol ComposeAppKotlinx_coroutines_coreChildJob <ComposeAppKotlinx_coroutines_coreJob>
@required
- (void)parentCancelledParentJob:(id<ComposeAppKotlinx_coroutines_coreParentJob>)parentJob __attribute__((swift_name("parentCancelled(parentJob:)")));
@end

__attribute__((swift_name("KotlinSequence")))
@protocol ComposeAppKotlinSequence
@required
- (id<ComposeAppKotlinIterator>)iterator __attribute__((swift_name("iterator()")));
@end

__attribute__((swift_name("Kotlinx_coroutines_coreSelectClause")))
@protocol ComposeAppKotlinx_coroutines_coreSelectClause
@required
@property (readonly) id clauseObject __attribute__((swift_name("clauseObject")));
@property (readonly) ComposeAppKotlinUnit *(^(^ _Nullable onCancellationConstructor)(id<ComposeAppKotlinx_coroutines_coreSelectInstance>, id _Nullable, id _Nullable))(ComposeAppKotlinThrowable *) __attribute__((swift_name("onCancellationConstructor")));
@property (readonly) id _Nullable (^processResFunc)(id, id _Nullable, id _Nullable) __attribute__((swift_name("processResFunc")));
@property (readonly) void (^regFunc)(id, id<ComposeAppKotlinx_coroutines_coreSelectInstance>, id _Nullable) __attribute__((swift_name("regFunc")));
@end

__attribute__((swift_name("Kotlinx_coroutines_coreSelectClause0")))
@protocol ComposeAppKotlinx_coroutines_coreSelectClause0 <ComposeAppKotlinx_coroutines_coreSelectClause>
@required
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_httpHttpStatusCode.Companion")))
@interface ComposeAppKtor_httpHttpStatusCodeCompanion : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKtor_httpHttpStatusCodeCompanion *shared __attribute__((swift_name("shared")));
- (ComposeAppKtor_httpHttpStatusCode *)fromValueValue:(int32_t)value __attribute__((swift_name("fromValue(value:)")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *Accepted __attribute__((swift_name("Accepted")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *BadGateway __attribute__((swift_name("BadGateway")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *BadRequest __attribute__((swift_name("BadRequest")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *Conflict __attribute__((swift_name("Conflict")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *Continue __attribute__((swift_name("Continue")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *Created __attribute__((swift_name("Created")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *ExpectationFailed __attribute__((swift_name("ExpectationFailed")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *FailedDependency __attribute__((swift_name("FailedDependency")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *Forbidden __attribute__((swift_name("Forbidden")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *Found __attribute__((swift_name("Found")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *GatewayTimeout __attribute__((swift_name("GatewayTimeout")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *Gone __attribute__((swift_name("Gone")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *InsufficientStorage __attribute__((swift_name("InsufficientStorage")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *InternalServerError __attribute__((swift_name("InternalServerError")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *LengthRequired __attribute__((swift_name("LengthRequired")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *Locked __attribute__((swift_name("Locked")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *MethodNotAllowed __attribute__((swift_name("MethodNotAllowed")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *MovedPermanently __attribute__((swift_name("MovedPermanently")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *MultiStatus __attribute__((swift_name("MultiStatus")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *MultipleChoices __attribute__((swift_name("MultipleChoices")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *NoContent __attribute__((swift_name("NoContent")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *NonAuthoritativeInformation __attribute__((swift_name("NonAuthoritativeInformation")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *NotAcceptable __attribute__((swift_name("NotAcceptable")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *NotFound __attribute__((swift_name("NotFound")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *NotImplemented __attribute__((swift_name("NotImplemented")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *NotModified __attribute__((swift_name("NotModified")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *OK __attribute__((swift_name("OK")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *PartialContent __attribute__((swift_name("PartialContent")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *PayloadTooLarge __attribute__((swift_name("PayloadTooLarge")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *PaymentRequired __attribute__((swift_name("PaymentRequired")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *PermanentRedirect __attribute__((swift_name("PermanentRedirect")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *PreconditionFailed __attribute__((swift_name("PreconditionFailed")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *Processing __attribute__((swift_name("Processing")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *ProxyAuthenticationRequired __attribute__((swift_name("ProxyAuthenticationRequired")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *RequestHeaderFieldTooLarge __attribute__((swift_name("RequestHeaderFieldTooLarge")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *RequestTimeout __attribute__((swift_name("RequestTimeout")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *RequestURITooLong __attribute__((swift_name("RequestURITooLong")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *RequestedRangeNotSatisfiable __attribute__((swift_name("RequestedRangeNotSatisfiable")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *ResetContent __attribute__((swift_name("ResetContent")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *SeeOther __attribute__((swift_name("SeeOther")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *ServiceUnavailable __attribute__((swift_name("ServiceUnavailable")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *SwitchProxy __attribute__((swift_name("SwitchProxy")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *SwitchingProtocols __attribute__((swift_name("SwitchingProtocols")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *TemporaryRedirect __attribute__((swift_name("TemporaryRedirect")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *TooEarly __attribute__((swift_name("TooEarly")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *TooManyRequests __attribute__((swift_name("TooManyRequests")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *Unauthorized __attribute__((swift_name("Unauthorized")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *UnprocessableEntity __attribute__((swift_name("UnprocessableEntity")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *UnsupportedMediaType __attribute__((swift_name("UnsupportedMediaType")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *UpgradeRequired __attribute__((swift_name("UpgradeRequired")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *UseProxy __attribute__((swift_name("UseProxy")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *VariantAlsoNegotiates __attribute__((swift_name("VariantAlsoNegotiates")));
@property (readonly) ComposeAppKtor_httpHttpStatusCode *VersionNotSupported __attribute__((swift_name("VersionNotSupported")));
@property (readonly) NSArray<ComposeAppKtor_httpHttpStatusCode *> *allStatusCodes __attribute__((swift_name("allStatusCodes")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_utilsGMTDate.Companion")))
@interface ComposeAppKtor_utilsGMTDateCompanion : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKtor_utilsGMTDateCompanion *shared __attribute__((swift_name("shared")));
@property (readonly) ComposeAppKtor_utilsGMTDate *START __attribute__((swift_name("START")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_utilsWeekDay")))
@interface ComposeAppKtor_utilsWeekDay : ComposeAppKotlinEnum<ComposeAppKtor_utilsWeekDay *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly, getter=companion) ComposeAppKtor_utilsWeekDayCompanion *companion __attribute__((swift_name("companion")));
@property (class, readonly) ComposeAppKtor_utilsWeekDay *monday __attribute__((swift_name("monday")));
@property (class, readonly) ComposeAppKtor_utilsWeekDay *tuesday __attribute__((swift_name("tuesday")));
@property (class, readonly) ComposeAppKtor_utilsWeekDay *wednesday __attribute__((swift_name("wednesday")));
@property (class, readonly) ComposeAppKtor_utilsWeekDay *thursday __attribute__((swift_name("thursday")));
@property (class, readonly) ComposeAppKtor_utilsWeekDay *friday __attribute__((swift_name("friday")));
@property (class, readonly) ComposeAppKtor_utilsWeekDay *saturday __attribute__((swift_name("saturday")));
@property (class, readonly) ComposeAppKtor_utilsWeekDay *sunday __attribute__((swift_name("sunday")));
+ (ComposeAppKotlinArray<ComposeAppKtor_utilsWeekDay *> *)values __attribute__((swift_name("values()")));
@property (readonly) NSString *value __attribute__((swift_name("value")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_utilsMonth")))
@interface ComposeAppKtor_utilsMonth : ComposeAppKotlinEnum<ComposeAppKtor_utilsMonth *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly, getter=companion) ComposeAppKtor_utilsMonthCompanion *companion __attribute__((swift_name("companion")));
@property (class, readonly) ComposeAppKtor_utilsMonth *january __attribute__((swift_name("january")));
@property (class, readonly) ComposeAppKtor_utilsMonth *february __attribute__((swift_name("february")));
@property (class, readonly) ComposeAppKtor_utilsMonth *march __attribute__((swift_name("march")));
@property (class, readonly) ComposeAppKtor_utilsMonth *april __attribute__((swift_name("april")));
@property (class, readonly) ComposeAppKtor_utilsMonth *may __attribute__((swift_name("may")));
@property (class, readonly) ComposeAppKtor_utilsMonth *june __attribute__((swift_name("june")));
@property (class, readonly) ComposeAppKtor_utilsMonth *july __attribute__((swift_name("july")));
@property (class, readonly) ComposeAppKtor_utilsMonth *august __attribute__((swift_name("august")));
@property (class, readonly) ComposeAppKtor_utilsMonth *september __attribute__((swift_name("september")));
@property (class, readonly) ComposeAppKtor_utilsMonth *october __attribute__((swift_name("october")));
@property (class, readonly) ComposeAppKtor_utilsMonth *november __attribute__((swift_name("november")));
@property (class, readonly) ComposeAppKtor_utilsMonth *december __attribute__((swift_name("december")));
+ (ComposeAppKotlinArray<ComposeAppKtor_utilsMonth *> *)values __attribute__((swift_name("values()")));
@property (readonly) NSString *value __attribute__((swift_name("value")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_httpHttpProtocolVersion.Companion")))
@interface ComposeAppKtor_httpHttpProtocolVersionCompanion : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKtor_httpHttpProtocolVersionCompanion *shared __attribute__((swift_name("shared")));
- (ComposeAppKtor_httpHttpProtocolVersion *)fromValueName:(NSString *)name major:(int32_t)major minor:(int32_t)minor __attribute__((swift_name("fromValue(name:major:minor:)")));
- (ComposeAppKtor_httpHttpProtocolVersion *)parseValue:(id)value __attribute__((swift_name("parse(value:)")));
@property (readonly) ComposeAppKtor_httpHttpProtocolVersion *HTTP_1_0 __attribute__((swift_name("HTTP_1_0")));
@property (readonly) ComposeAppKtor_httpHttpProtocolVersion *HTTP_1_1 __attribute__((swift_name("HTTP_1_1")));
@property (readonly) ComposeAppKtor_httpHttpProtocolVersion *HTTP_2_0 __attribute__((swift_name("HTTP_2_0")));
@property (readonly) ComposeAppKtor_httpHttpProtocolVersion *QUIC __attribute__((swift_name("QUIC")));
@property (readonly) ComposeAppKtor_httpHttpProtocolVersion *SPDY_3 __attribute__((swift_name("SPDY_3")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_ioMemory")))
@interface ComposeAppKtor_ioMemory : ComposeAppBase
- (instancetype)initWithPointer:(void *)pointer size:(int64_t)size __attribute__((swift_name("init(pointer:size:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) ComposeAppKtor_ioMemoryCompanion *companion __attribute__((swift_name("companion")));
- (void)doCopyToDestination:(ComposeAppKtor_ioMemory *)destination offset:(int32_t)offset length:(int32_t)length destinationOffset:(int32_t)destinationOffset __attribute__((swift_name("doCopyTo(destination:offset:length:destinationOffset:)")));
- (void)doCopyToDestination:(ComposeAppKtor_ioMemory *)destination offset:(int64_t)offset length:(int64_t)length destinationOffset_:(int64_t)destinationOffset __attribute__((swift_name("doCopyTo(destination:offset:length:destinationOffset_:)")));
- (int8_t)loadAtIndex:(int32_t)index __attribute__((swift_name("loadAt(index:)")));
- (int8_t)loadAtIndex_:(int64_t)index __attribute__((swift_name("loadAt(index_:)")));
- (ComposeAppKtor_ioMemory *)sliceOffset:(int32_t)offset length:(int32_t)length __attribute__((swift_name("slice(offset:length:)")));
- (ComposeAppKtor_ioMemory *)sliceOffset:(int64_t)offset length_:(int64_t)length __attribute__((swift_name("slice(offset:length_:)")));
- (void)storeAtIndex:(int32_t)index value:(int8_t)value __attribute__((swift_name("storeAt(index:value:)")));
- (void)storeAtIndex:(int64_t)index value_:(int8_t)value __attribute__((swift_name("storeAt(index:value_:)")));
@property (readonly) void *pointer __attribute__((swift_name("pointer")));
@property (readonly) int64_t size __attribute__((swift_name("size")));
@property (readonly) int32_t size32 __attribute__((swift_name("size32")));
@end

__attribute__((swift_name("Ktor_ioBuffer")))
@interface ComposeAppKtor_ioBuffer : ComposeAppBase
- (instancetype)initWithMemory:(ComposeAppKtor_ioMemory *)memory __attribute__((swift_name("init(memory:)"))) __attribute__((objc_designated_initializer)) __attribute__((deprecated("\n    We're migrating to the new kotlinx-io library.\n    This declaration is deprecated and will be removed in Ktor 4.0.0\n    If you have any problems with migration, please contact us in \n    https://youtrack.jetbrains.com/issue/KTOR-6030/Migrate-to-new-kotlinx.io-library\n    ")));
@property (class, readonly, getter=companion) ComposeAppKtor_ioBufferCompanion *companion __attribute__((swift_name("companion")));
- (void)commitWrittenCount:(int32_t)count __attribute__((swift_name("commitWritten(count:)")));
- (void)discardExactCount:(int32_t)count __attribute__((swift_name("discardExact(count:)")));
- (ComposeAppKtor_ioBuffer *)duplicate __attribute__((swift_name("duplicate()")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)duplicateToCopy:(ComposeAppKtor_ioBuffer *)copy __attribute__((swift_name("duplicateTo(copy:)")));
- (int8_t)readByte __attribute__((swift_name("readByte()")));
- (void)reserveEndGapEndGap:(int32_t)endGap __attribute__((swift_name("reserveEndGap(endGap:)")));
- (void)reserveStartGapStartGap:(int32_t)startGap __attribute__((swift_name("reserveStartGap(startGap:)")));
- (void)reset __attribute__((swift_name("reset()")));
- (void)resetForRead __attribute__((swift_name("resetForRead()")));
- (void)resetForWrite __attribute__((swift_name("resetForWrite()")));
- (void)resetForWriteLimit:(int32_t)limit __attribute__((swift_name("resetForWrite(limit:)")));
- (void)rewindCount:(int32_t)count __attribute__((swift_name("rewind(count:)")));
- (NSString *)description __attribute__((swift_name("description()")));
- (int32_t)tryPeekByte __attribute__((swift_name("tryPeekByte()")));
- (int32_t)tryReadByte __attribute__((swift_name("tryReadByte()")));
- (void)writeByteValue:(int8_t)value __attribute__((swift_name("writeByte(value:)")));
@property (readonly) int32_t capacity __attribute__((swift_name("capacity")));
@property (readonly) int32_t endGap __attribute__((swift_name("endGap")));
@property (readonly) int32_t limit __attribute__((swift_name("limit")));
@property (readonly) ComposeAppKtor_ioMemory *memory __attribute__((swift_name("memory")));
@property (readonly) int32_t readPosition __attribute__((swift_name("readPosition")));
@property (readonly) int32_t readRemaining __attribute__((swift_name("readRemaining")));
@property (readonly) int32_t startGap __attribute__((swift_name("startGap")));
@property (readonly) int32_t writePosition __attribute__((swift_name("writePosition")));
@property (readonly) int32_t writeRemaining __attribute__((swift_name("writeRemaining")));
@end

__attribute__((swift_name("Ktor_ioChunkBuffer")))
@interface ComposeAppKtor_ioChunkBuffer : ComposeAppKtor_ioBuffer
- (instancetype)initWithMemory:(ComposeAppKtor_ioMemory *)memory origin:(ComposeAppKtor_ioChunkBuffer * _Nullable)origin parentPool:(id<ComposeAppKtor_ioObjectPool> _Nullable)parentPool __attribute__((swift_name("init(memory:origin:parentPool:)"))) __attribute__((objc_designated_initializer)) __attribute__((deprecated("\n    We're migrating to the new kotlinx-io library.\n    This declaration is deprecated and will be removed in Ktor 4.0.0\n    If you have any problems with migration, please contact us in \n    https://youtrack.jetbrains.com/issue/KTOR-6030/Migrate-to-new-kotlinx.io-library\n    ")));
- (instancetype)initWithMemory:(ComposeAppKtor_ioMemory *)memory __attribute__((swift_name("init(memory:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly, getter=companion) ComposeAppKtor_ioChunkBufferCompanion *companion __attribute__((swift_name("companion")));
- (ComposeAppKtor_ioChunkBuffer * _Nullable)cleanNext __attribute__((swift_name("cleanNext()")));
- (ComposeAppKtor_ioChunkBuffer *)duplicate __attribute__((swift_name("duplicate()")));
- (void)releasePool:(id<ComposeAppKtor_ioObjectPool>)pool __attribute__((swift_name("release(pool:)")));
- (void)reset __attribute__((swift_name("reset()")));
@property (getter=next_) ComposeAppKtor_ioChunkBuffer * _Nullable next __attribute__((swift_name("next")));
@property (readonly) ComposeAppKtor_ioChunkBuffer * _Nullable origin __attribute__((swift_name("origin")));
@property (readonly) int32_t referenceCount __attribute__((swift_name("referenceCount")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinByteArray")))
@interface ComposeAppKotlinByteArray : ComposeAppBase
+ (instancetype)arrayWithSize:(int32_t)size __attribute__((swift_name("init(size:)")));
+ (instancetype)arrayWithSize:(int32_t)size init:(ComposeAppByte *(^)(ComposeAppInt *))init __attribute__((swift_name("init(size:init:)")));
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (int8_t)getIndex:(int32_t)index __attribute__((swift_name("get(index:)")));
- (ComposeAppKotlinByteIterator *)iterator __attribute__((swift_name("iterator()")));
- (void)setIndex:(int32_t)index value:(int8_t)value __attribute__((swift_name("set(index:value:)")));
@property (readonly) int32_t size __attribute__((swift_name("size")));
@end

__attribute__((swift_name("Ktor_ioInput")))
@interface ComposeAppKtor_ioInput : ComposeAppBase <ComposeAppKtor_ioCloseable>
- (instancetype)initWithHead:(ComposeAppKtor_ioChunkBuffer *)head remaining:(int64_t)remaining pool:(id<ComposeAppKtor_ioObjectPool>)pool __attribute__((swift_name("init(head:remaining:pool:)"))) __attribute__((objc_designated_initializer)) __attribute__((deprecated("\n    We're migrating to the new kotlinx-io library.\n    This declaration is deprecated and will be removed in Ktor 4.0.0\n    If you have any problems with migration, please contact us in \n    https://youtrack.jetbrains.com/issue/KTOR-6030/Migrate-to-new-kotlinx.io-library\n    ")));
@property (class, readonly, getter=companion) ComposeAppKtor_ioInputCompanion *companion __attribute__((swift_name("companion")));
- (BOOL)canRead __attribute__((swift_name("canRead()")));
- (void)close __attribute__((swift_name("close()")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)closeSource __attribute__((swift_name("closeSource()")));
- (int32_t)discardN:(int32_t)n __attribute__((swift_name("discard(n:)")));
- (int64_t)discardN_:(int64_t)n __attribute__((swift_name("discard(n_:)")));
- (void)discardExactN:(int32_t)n __attribute__((swift_name("discardExact(n:)")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (ComposeAppKtor_ioChunkBuffer * _Nullable)fill __attribute__((swift_name("fill()")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (int32_t)fillDestination:(ComposeAppKtor_ioMemory *)destination offset:(int32_t)offset length:(int32_t)length __attribute__((swift_name("fill(destination:offset:length:)")));
- (BOOL)hasBytesN:(int32_t)n __attribute__((swift_name("hasBytes(n:)")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)markNoMoreChunksAvailable __attribute__((swift_name("markNoMoreChunksAvailable()")));
- (int32_t)peekToBuffer:(ComposeAppKtor_ioChunkBuffer *)buffer __attribute__((swift_name("peekTo(buffer:)")));
- (int64_t)peekToDestination:(ComposeAppKtor_ioMemory *)destination destinationOffset:(int64_t)destinationOffset offset:(int64_t)offset min:(int64_t)min max:(int64_t)max __attribute__((swift_name("peekTo(destination:destinationOffset:offset:min:max:)")));
- (int8_t)readByte __attribute__((swift_name("readByte()")));
- (NSString *)readTextMin:(int32_t)min max:(int32_t)max __attribute__((swift_name("readText(min:max:)")));
- (int32_t)readTextOut:(id<ComposeAppKotlinAppendable>)out min:(int32_t)min max:(int32_t)max __attribute__((swift_name("readText(out:min:max:)")));
- (NSString *)readTextExactExactCharacters:(int32_t)exactCharacters __attribute__((swift_name("readTextExact(exactCharacters:)")));
- (void)readTextExactOut:(id<ComposeAppKotlinAppendable>)out exactCharacters:(int32_t)exactCharacters __attribute__((swift_name("readTextExact(out:exactCharacters:)")));
- (void)release_ __attribute__((swift_name("release()")));
- (int32_t)tryPeek __attribute__((swift_name("tryPeek()")));
@property (readonly) BOOL endOfInput __attribute__((swift_name("endOfInput")));
@property (readonly) id<ComposeAppKtor_ioObjectPool> pool __attribute__((swift_name("pool")));
@property (readonly) int64_t remaining __attribute__((swift_name("remaining")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_ioByteReadPacket")))
@interface ComposeAppKtor_ioByteReadPacket : ComposeAppKtor_ioInput
- (instancetype)initWithHead:(ComposeAppKtor_ioChunkBuffer *)head pool:(id<ComposeAppKtor_ioObjectPool>)pool __attribute__((swift_name("init(head:pool:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithHead:(ComposeAppKtor_ioChunkBuffer *)head remaining:(int64_t)remaining pool:(id<ComposeAppKtor_ioObjectPool>)pool __attribute__((swift_name("init(head:remaining:pool:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly, getter=companion) ComposeAppKtor_ioByteReadPacketCompanion *companion __attribute__((swift_name("companion")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (void)closeSource __attribute__((swift_name("closeSource()")));
- (ComposeAppKtor_ioByteReadPacket *)doCopy __attribute__((swift_name("doCopy()")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (ComposeAppKtor_ioChunkBuffer * _Nullable)fill __attribute__((swift_name("fill()")));

/**
 * @note This method has protected visibility in Kotlin source and is intended only for use by subclasses.
*/
- (int32_t)fillDestination:(ComposeAppKtor_ioMemory *)destination offset:(int32_t)offset length:(int32_t)length __attribute__((swift_name("fill(destination:offset:length:)")));
- (NSString *)description __attribute__((swift_name("description()")));
@end

__attribute__((swift_name("Ktor_ioReadSession")))
@protocol ComposeAppKtor_ioReadSession
@required
- (int32_t)discardN:(int32_t)n __attribute__((swift_name("discard(n:)")));
- (ComposeAppKtor_ioChunkBuffer * _Nullable)requestAtLeast:(int32_t)atLeast __attribute__((swift_name("request(atLeast:)")));
@property (readonly) int32_t availableForRead __attribute__((swift_name("availableForRead")));
@end

__attribute__((swift_name("KotlinSuspendFunction1")))
@protocol ComposeAppKotlinSuspendFunction1 <ComposeAppKotlinFunction>
@required

/**
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)invokeP1:(id _Nullable)p1 completionHandler:(void (^)(id _Nullable_result, NSError * _Nullable))completionHandler __attribute__((swift_name("invoke(p1:completionHandler:)")));
@end

__attribute__((swift_name("KotlinAppendable")))
@protocol ComposeAppKotlinAppendable
@required
- (id<ComposeAppKotlinAppendable>)appendValue:(unichar)value __attribute__((swift_name("append(value:)")));
- (id<ComposeAppKotlinAppendable>)appendValue_:(id _Nullable)value __attribute__((swift_name("append(value_:)")));
- (id<ComposeAppKotlinAppendable>)appendValue:(id _Nullable)value startIndex:(int32_t)startIndex endIndex:(int32_t)endIndex __attribute__((swift_name("append(value:startIndex:endIndex:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_httpURLBuilder.Companion")))
@interface ComposeAppKtor_httpURLBuilderCompanion : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKtor_httpURLBuilderCompanion *shared __attribute__((swift_name("shared")));
@end

__attribute__((swift_name("Ktor_httpParametersBuilder")))
@protocol ComposeAppKtor_httpParametersBuilder <ComposeAppKtor_utilsStringValuesBuilder>
@required
@end

__attribute__((swift_name("KotlinKDeclarationContainer")))
@protocol ComposeAppKotlinKDeclarationContainer
@required
@end

__attribute__((swift_name("KotlinKAnnotatedElement")))
@protocol ComposeAppKotlinKAnnotatedElement
@required
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.1")
*/
__attribute__((swift_name("KotlinKClassifier")))
@protocol ComposeAppKotlinKClassifier
@required
@end

__attribute__((swift_name("KotlinKClass")))
@protocol ComposeAppKotlinKClass <ComposeAppKotlinKDeclarationContainer, ComposeAppKotlinKAnnotatedElement, ComposeAppKotlinKClassifier>
@required

/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.1")
*/
- (BOOL)isInstanceValue:(id _Nullable)value __attribute__((swift_name("isInstance(value:)")));
@property (readonly) NSString * _Nullable qualifiedName __attribute__((swift_name("qualifiedName")));
@property (readonly) NSString * _Nullable simpleName __attribute__((swift_name("simpleName")));
@end

__attribute__((swift_name("KotlinKType")))
@protocol ComposeAppKotlinKType
@required

/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.1")
*/
@property (readonly) NSArray<ComposeAppKotlinKTypeProjection *> *arguments __attribute__((swift_name("arguments")));

/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.1")
*/
@property (readonly) id<ComposeAppKotlinKClassifier> _Nullable classifier __attribute__((swift_name("classifier")));
@property (readonly) BOOL isMarkedNullable __attribute__((swift_name("isMarkedNullable")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_httpURLProtocol.Companion")))
@interface ComposeAppKtor_httpURLProtocolCompanion : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKtor_httpURLProtocolCompanion *shared __attribute__((swift_name("shared")));
- (ComposeAppKtor_httpURLProtocol *)createOrDefaultName:(NSString *)name __attribute__((swift_name("createOrDefault(name:)")));
@property (readonly) ComposeAppKtor_httpURLProtocol *HTTP __attribute__((swift_name("HTTP")));
@property (readonly) ComposeAppKtor_httpURLProtocol *HTTPS __attribute__((swift_name("HTTPS")));
@property (readonly) ComposeAppKtor_httpURLProtocol *SOCKS __attribute__((swift_name("SOCKS")));
@property (readonly) ComposeAppKtor_httpURLProtocol *WS __attribute__((swift_name("WS")));
@property (readonly) ComposeAppKtor_httpURLProtocol *WSS __attribute__((swift_name("WSS")));
@property (readonly) NSDictionary<NSString *, ComposeAppKtor_httpURLProtocol *> *byName __attribute__((swift_name("byName")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_httpHeaderValueParam")))
@interface ComposeAppKtor_httpHeaderValueParam : ComposeAppBase
- (instancetype)initWithName:(NSString *)name value:(NSString *)value __attribute__((swift_name("init(name:value:)"))) __attribute__((objc_designated_initializer));
- (instancetype)initWithName:(NSString *)name value:(NSString *)value escapeValue:(BOOL)escapeValue __attribute__((swift_name("init(name:value:escapeValue:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppKtor_httpHeaderValueParam *)doCopyName:(NSString *)name value:(NSString *)value escapeValue:(BOOL)escapeValue __attribute__((swift_name("doCopy(name:value:escapeValue:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) BOOL escapeValue __attribute__((swift_name("escapeValue")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@property (readonly) NSString *value __attribute__((swift_name("value")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_httpHeaderValueWithParameters.Companion")))
@interface ComposeAppKtor_httpHeaderValueWithParametersCompanion : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKtor_httpHeaderValueWithParametersCompanion *shared __attribute__((swift_name("shared")));
- (id _Nullable)parseValue:(NSString *)value init:(id _Nullable (^)(NSString *, NSArray<ComposeAppKtor_httpHeaderValueParam *> *))init __attribute__((swift_name("parse(value:init:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_httpContentType.Companion")))
@interface ComposeAppKtor_httpContentTypeCompanion : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKtor_httpContentTypeCompanion *shared __attribute__((swift_name("shared")));
- (ComposeAppKtor_httpContentType *)parseValue:(NSString *)value __attribute__((swift_name("parse(value:)")));
@property (readonly) ComposeAppKtor_httpContentType *Any __attribute__((swift_name("Any")));
@end

__attribute__((swift_name("Kotlinx_coroutines_coreParentJob")))
@protocol ComposeAppKotlinx_coroutines_coreParentJob <ComposeAppKotlinx_coroutines_coreJob>
@required
- (ComposeAppKotlinCancellationException *)getChildJobCancellationCause __attribute__((swift_name("getChildJobCancellationCause()")));
@end

__attribute__((swift_name("Kotlinx_coroutines_coreSelectInstance")))
@protocol ComposeAppKotlinx_coroutines_coreSelectInstance
@required
- (void)disposeOnCompletionDisposableHandle:(id<ComposeAppKotlinx_coroutines_coreDisposableHandle>)disposableHandle __attribute__((swift_name("disposeOnCompletion(disposableHandle:)")));
- (void)selectInRegistrationPhaseInternalResult:(id _Nullable)internalResult __attribute__((swift_name("selectInRegistrationPhase(internalResult:)")));
- (BOOL)trySelectClauseObject:(id)clauseObject result:(id _Nullable)result __attribute__((swift_name("trySelect(clauseObject:result:)")));
@property (readonly) id<ComposeAppKotlinCoroutineContext> context __attribute__((swift_name("context")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_utilsWeekDay.Companion")))
@interface ComposeAppKtor_utilsWeekDayCompanion : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKtor_utilsWeekDayCompanion *shared __attribute__((swift_name("shared")));
- (ComposeAppKtor_utilsWeekDay *)fromOrdinal:(int32_t)ordinal __attribute__((swift_name("from(ordinal:)")));
- (ComposeAppKtor_utilsWeekDay *)fromValue:(NSString *)value __attribute__((swift_name("from(value:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_utilsMonth.Companion")))
@interface ComposeAppKtor_utilsMonthCompanion : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKtor_utilsMonthCompanion *shared __attribute__((swift_name("shared")));
- (ComposeAppKtor_utilsMonth *)fromOrdinal:(int32_t)ordinal __attribute__((swift_name("from(ordinal:)")));
- (ComposeAppKtor_utilsMonth *)fromValue:(NSString *)value __attribute__((swift_name("from(value:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_ioMemory.Companion")))
@interface ComposeAppKtor_ioMemoryCompanion : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKtor_ioMemoryCompanion *shared __attribute__((swift_name("shared")));
@property (readonly) ComposeAppKtor_ioMemory *Empty __attribute__((swift_name("Empty")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_ioBuffer.Companion")))
@interface ComposeAppKtor_ioBufferCompanion : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKtor_ioBufferCompanion *shared __attribute__((swift_name("shared")));
@property (readonly) ComposeAppKtor_ioBuffer *Empty __attribute__((swift_name("Empty")));
@property (readonly) int32_t ReservedSize __attribute__((swift_name("ReservedSize")));
@end

__attribute__((swift_name("Ktor_ioObjectPool")))
@protocol ComposeAppKtor_ioObjectPool <ComposeAppKtor_ioCloseable>
@required
- (id)borrow __attribute__((swift_name("borrow()")));
- (void)dispose __attribute__((swift_name("dispose()")));
- (void)recycleInstance:(id)instance __attribute__((swift_name("recycle(instance:)")));
@property (readonly) int32_t capacity __attribute__((swift_name("capacity")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_ioChunkBuffer.Companion")))
@interface ComposeAppKtor_ioChunkBufferCompanion : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKtor_ioChunkBufferCompanion *shared __attribute__((swift_name("shared")));
@property (readonly) ComposeAppKtor_ioChunkBuffer *Empty __attribute__((swift_name("Empty")));
@property (readonly) id<ComposeAppKtor_ioObjectPool> EmptyPool __attribute__((swift_name("EmptyPool")));
@property (readonly) id<ComposeAppKtor_ioObjectPool> Pool __attribute__((swift_name("Pool")));
@end

__attribute__((swift_name("KotlinByteIterator")))
@interface ComposeAppKotlinByteIterator : ComposeAppBase <ComposeAppKotlinIterator>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (ComposeAppByte *)next __attribute__((swift_name("next()")));
- (int8_t)nextByte __attribute__((swift_name("nextByte()")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_ioInput.Companion")))
@interface ComposeAppKtor_ioInputCompanion : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKtor_ioInputCompanion *shared __attribute__((swift_name("shared")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Ktor_ioByteReadPacket.Companion")))
@interface ComposeAppKtor_ioByteReadPacketCompanion : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKtor_ioByteReadPacketCompanion *shared __attribute__((swift_name("shared")));
@property (readonly) ComposeAppKtor_ioByteReadPacket *Empty __attribute__((swift_name("Empty")));
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.1")
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinKTypeProjection")))
@interface ComposeAppKotlinKTypeProjection : ComposeAppBase
- (instancetype)initWithVariance:(ComposeAppKotlinKVariance * _Nullable)variance type:(id<ComposeAppKotlinKType> _Nullable)type __attribute__((swift_name("init(variance:type:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) ComposeAppKotlinKTypeProjectionCompanion *companion __attribute__((swift_name("companion")));
- (ComposeAppKotlinKTypeProjection *)doCopyVariance:(ComposeAppKotlinKVariance * _Nullable)variance type:(id<ComposeAppKotlinKType> _Nullable)type __attribute__((swift_name("doCopy(variance:type:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) id<ComposeAppKotlinKType> _Nullable type __attribute__((swift_name("type")));
@property (readonly) ComposeAppKotlinKVariance * _Nullable variance __attribute__((swift_name("variance")));
@end


/**
 * @note annotations
 *   kotlin.SinceKotlin(version="1.1")
*/
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinKVariance")))
@interface ComposeAppKotlinKVariance : ComposeAppKotlinEnum<ComposeAppKotlinKVariance *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) ComposeAppKotlinKVariance *invariant __attribute__((swift_name("invariant")));
@property (class, readonly) ComposeAppKotlinKVariance *in __attribute__((swift_name("in")));
@property (class, readonly) ComposeAppKotlinKVariance *out __attribute__((swift_name("out")));
+ (ComposeAppKotlinArray<ComposeAppKotlinKVariance *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<ComposeAppKotlinKVariance *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinKTypeProjection.Companion")))
@interface ComposeAppKotlinKTypeProjectionCompanion : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppKotlinKTypeProjectionCompanion *shared __attribute__((swift_name("shared")));

/**
 * @note annotations
 *   kotlin.jvm.JvmStatic
*/
- (ComposeAppKotlinKTypeProjection *)contravariantType:(id<ComposeAppKotlinKType>)type __attribute__((swift_name("contravariant(type:)")));

/**
 * @note annotations
 *   kotlin.jvm.JvmStatic
*/
- (ComposeAppKotlinKTypeProjection *)covariantType:(id<ComposeAppKotlinKType>)type __attribute__((swift_name("covariant(type:)")));

/**
 * @note annotations
 *   kotlin.jvm.JvmStatic
*/
- (ComposeAppKotlinKTypeProjection *)invariantType:(id<ComposeAppKotlinKType>)type __attribute__((swift_name("invariant(type:)")));
@property (readonly) ComposeAppKotlinKTypeProjection *STAR __attribute__((swift_name("STAR")));
@end

#pragma pop_macro("_Nullable_result")
#pragma clang diagnostic pop
NS_ASSUME_NONNULL_END
