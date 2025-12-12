#import <Foundation/NSArray.h>
#import <Foundation/NSDictionary.h>
#import <Foundation/NSError.h>
#import <Foundation/NSObject.h>
#import <Foundation/NSSet.h>
#import <Foundation/NSString.h>
#import <Foundation/NSValue.h>

@class ComposeAppAction, ComposeAppActionAccept, ComposeAppActionBack, ComposeAppActionComplete, ComposeAppActionDecline, ComposeAppActionStart, ComposeAppActions, ComposeAppCondition, ComposeAppDeepdots, ComposeAppDeepdotsPopups, ComposeAppEvent, ComposeAppEventData, ComposeAppEvents, ComposeAppHtmlParagraph, ComposeAppHtmlRun, ComposeAppImageAlignment, ComposeAppImageSize, ComposeAppInitOptions, ComposeAppKotlinArray<T>, ComposeAppKotlinEnum<E>, ComposeAppKotlinEnumCompanion, ComposeAppKotlinException, ComposeAppKotlinIllegalStateException, ComposeAppKotlinRuntimeException, ComposeAppKotlinThrowable, ComposeAppMode, ComposeAppPlatformContext, ComposeAppPopupDefinition, ComposeAppPopupOptions, ComposeAppPopupRenderer, ComposeAppPosition, ComposeAppSegments, ComposeAppShowOptions, ComposeAppStyle, ComposeAppTheme, ComposeAppTrigger, ComposeAppTriggerExit, ComposeAppTriggerScroll, ComposeAppTriggerTimeOnPage, UIViewController;

@protocol ComposeAppKeyValueStorage, ComposeAppKotlinComparable, ComposeAppKotlinIterator, ComposeAppPlatform;

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
 * Objeto de conveniencia para crear y configurar una instancia del SDK.
 */
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Deepdots")))
@interface ComposeAppDeepdots : ComposeAppBase
+ (instancetype)alloc __attribute__((unavailable));

/**
 * Objeto de conveniencia para crear y configurar una instancia del SDK.
 */
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)deepdots __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) ComposeAppDeepdots *shared __attribute__((swift_name("shared")));

/** Crea una instancia vacía (requiere llamar a init) */
- (ComposeAppDeepdotsPopups *)create __attribute__((swift_name("create()")));

/** Crea e inicializa una instancia en un paso. */
- (ComposeAppDeepdotsPopups *)createInitializedOptions:(ComposeAppInitOptions *)options __attribute__((swift_name("createInitialized(options:)")));

/**
 * Simple helper for Swift/ObjC interop: create and init SDK with a single popup without referencing Kotlin data classes from Swift.
 */
- (ComposeAppDeepdotsPopups *)createInitializedSimpleId:(NSString *)id title:(NSString *)title messageHtml:(NSString *)messageHtml surveyId:(NSString *)surveyId productId:(NSString *)productId triggerSeconds:(int32_t)triggerSeconds acceptLabel:(NSString *)acceptLabel declineLabel:(NSString *)declineLabel declineCooldownDays:(int32_t)declineCooldownDays debug:(BOOL)debug autoLaunch:(BOOL)autoLaunch lang:(NSString * _Nullable)lang path:(NSString * _Nullable)path __attribute__((swift_name("createInitializedSimple(id:title:messageHtml:surveyId:productId:triggerSeconds:acceptLabel:declineLabel:declineCooldownDays:debug:autoLaunch:lang:path:)")));

/** Dismiss manual (reexport). */
- (void)dismissContext:(ComposeAppPlatformContext *)context __attribute__((swift_name("dismiss(context:)")));

/**
 * Devuelve el HTML completo para renderizar la encuesta MagicFeedback (incluye loader y fallback CDN).
 * Útil para que la app iOS/Android lo cargue directamente en su WebView/WKWebView.
 */
- (NSString *)getSurveyHtmlSurveyId:(NSString *)surveyId productId:(NSString *)productId __attribute__((swift_name("getSurveyHtml(surveyId:productId:)")));

/** Timestamp utilitario (reexport). */
- (int64_t)now __attribute__((swift_name("now()")));

/** Parseo HTML básico (reexport). */
- (NSArray<ComposeAppHtmlParagraph *> *)parseHtmlHtml:(NSString *)html __attribute__((swift_name("parseHtml(html:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("DeepdotsPopups")))
@interface ComposeAppDeepdotsPopups : ComposeAppBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)attachContextContext:(ComposeAppPlatformContext *)context __attribute__((swift_name("attachContext(context:)")));

/** Permite cierre manual desde host (si hay popup activo). */
- (void)closeContext:(ComposeAppPlatformContext *)context __attribute__((swift_name("close(context:)")));

/**
 * Inicializar el SDK
 */
- (void)doInitOptions:(ComposeAppInitOptions *)options __attribute__((swift_name("doInit(options:)")));

/** Public initializer alias to avoid Swift bridging conflict with `init` constructor */
- (void)initializeOptions:(ComposeAppInitOptions *)options __attribute__((swift_name("initialize(options:)")));
- (void)markSurveyAnsweredSurveyId:(NSString *)surveyId __attribute__((swift_name("markSurveyAnswered(surveyId:)")));

/**
 * Registrar listeners
 */
- (void)onEvent:(ComposeAppEvent *)event listener:(void (^)(ComposeAppEventData *))listener __attribute__((swift_name("on(event:listener:)")));
- (void)setPathPath:(NSString * _Nullable)path __attribute__((swift_name("setPath(path:)")));

/**
 * Mostrar un popup desde la app anfitriona
 */
- (void)showOptions:(ComposeAppShowOptions *)options context:(ComposeAppPlatformContext *)context __attribute__((swift_name("show(options:context:)")));
- (void)showByPopupIdPopupId:(NSString *)popupId context:(ComposeAppPlatformContext *)context __attribute__((swift_name("showByPopupId(popupId:context:)")));
- (void)surveyCompletedFromJsSurveyId:(NSString *)surveyId __attribute__((swift_name("surveyCompletedFromJs(surveyId:)")));

/**
 * Stub que se completará en Tareas de triggers automáticos.
 */
- (void)triggerSurveySurveyId:(NSString *)surveyId context:(ComposeAppPlatformContext *)context __attribute__((swift_name("triggerSurvey(surveyId:context:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("EventBus")))
@interface ComposeAppEventBus : ComposeAppBase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));

/** Limpia todos los listeners.
 *
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)clearWithCompletionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("clear(completionHandler:)")));

/** Emite un evento a todos sus listeners (thread-safe).
 *
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)emitEvent:(ComposeAppEvent *)event data:(ComposeAppEventData *)data completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("emit(event:data:completionHandler:)")));

/** Quita un listener previamente registrado.
 *
 * @note This method converts instances of CancellationException to errors.
 * Other uncaught Kotlin exceptions are fatal.
*/
- (void)offEvent:(ComposeAppEvent *)event listener:(void (^)(ComposeAppEventData *))listener completionHandler:(void (^)(NSError * _Nullable))completionHandler __attribute__((swift_name("off(event:listener:completionHandler:)")));

/** Registra un listener para un evento (thread-safe).
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
__attribute__((swift_name("Condition")))
@interface ComposeAppCondition : ComposeAppBase
- (instancetype)initWithAnswered:(BOOL)answered cooldownDays:(int32_t)cooldownDays __attribute__((swift_name("init(answered:cooldownDays:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppCondition *)doCopyAnswered:(BOOL)answered cooldownDays:(int32_t)cooldownDays __attribute__((swift_name("doCopy(answered:cooldownDays:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) BOOL answered __attribute__((swift_name("answered")));
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
- (instancetype)initWithDebug:(ComposeAppBoolean * _Nullable)debug mode:(ComposeAppMode * _Nullable)mode popupOptions:(ComposeAppPopupOptions *)popupOptions provideLang:(NSString * _Nullable (^)(void))provideLang autoLaunch:(ComposeAppBoolean * _Nullable)autoLaunch storage:(id<ComposeAppKeyValueStorage> _Nullable)storage metadata:(NSDictionary<NSString *, id> * _Nullable)metadata __attribute__((swift_name("init(debug:mode:popupOptions:provideLang:autoLaunch:storage:metadata:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppInitOptions *)doCopyDebug:(ComposeAppBoolean * _Nullable)debug mode:(ComposeAppMode * _Nullable)mode popupOptions:(ComposeAppPopupOptions *)popupOptions provideLang:(NSString * _Nullable (^)(void))provideLang autoLaunch:(ComposeAppBoolean * _Nullable)autoLaunch storage:(id<ComposeAppKeyValueStorage> _Nullable)storage metadata:(NSDictionary<NSString *, id> * _Nullable)metadata __attribute__((swift_name("doCopy(debug:mode:popupOptions:provideLang:autoLaunch:storage:metadata:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) ComposeAppBoolean * _Nullable autoLaunch __attribute__((swift_name("autoLaunch")));
@property (readonly) ComposeAppBoolean * _Nullable debug __attribute__((swift_name("debug")));
@property (readonly) NSDictionary<NSString *, id> * _Nullable metadata __attribute__((swift_name("metadata")));
@property (readonly) ComposeAppMode * _Nullable mode __attribute__((swift_name("mode")));
@property (readonly) ComposeAppPopupOptions *popupOptions __attribute__((swift_name("popupOptions")));
@property (readonly) NSString * _Nullable (^provideLang)(void) __attribute__((swift_name("provideLang")));
@property (readonly) id<ComposeAppKeyValueStorage> _Nullable storage __attribute__((swift_name("storage")));
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
- (instancetype)initWithId:(NSString *)id title:(NSString *)title message:(NSString *)message trigger:(ComposeAppTrigger *)trigger conditions:(NSArray<ComposeAppCondition *> * _Nullable)conditions actions:(ComposeAppActions *)actions surveyId:(NSString *)surveyId productId:(NSString *)productId style:(ComposeAppStyle *)style segments:(ComposeAppSegments * _Nullable)segments __attribute__((swift_name("init(id:title:message:trigger:conditions:actions:surveyId:productId:style:segments:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppPopupDefinition *)doCopyId:(NSString *)id title:(NSString *)title message:(NSString *)message trigger:(ComposeAppTrigger *)trigger conditions:(NSArray<ComposeAppCondition *> * _Nullable)conditions actions:(ComposeAppActions *)actions surveyId:(NSString *)surveyId productId:(NSString *)productId style:(ComposeAppStyle *)style segments:(ComposeAppSegments * _Nullable)segments __attribute__((swift_name("doCopy(id:title:message:trigger:conditions:actions:surveyId:productId:style:segments:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) ComposeAppActions *actions __attribute__((swift_name("actions")));
@property (readonly) NSArray<ComposeAppCondition *> * _Nullable conditions __attribute__((swift_name("conditions")));
@property (readonly) NSString *id __attribute__((swift_name("id")));
@property (readonly) NSString *message __attribute__((swift_name("message")));
@property (readonly) NSString *productId __attribute__((swift_name("productId")));
@property (readonly) ComposeAppSegments * _Nullable segments __attribute__((swift_name("segments")));
@property (readonly) ComposeAppStyle *style __attribute__((swift_name("style")));
@property (readonly) NSString *surveyId __attribute__((swift_name("surveyId")));
@property (readonly) NSString *title __attribute__((swift_name("title")));
@property (readonly) ComposeAppTrigger *trigger __attribute__((swift_name("trigger")));
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
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Trigger.Exit")))
@interface ComposeAppTriggerExit : ComposeAppTrigger
- (instancetype)initWithCondition:(NSArray<ComposeAppCondition *> *)condition __attribute__((swift_name("init(condition:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppTriggerExit *)doCopyCondition:(NSArray<ComposeAppCondition *> *)condition __attribute__((swift_name("doCopy(condition:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSArray<ComposeAppCondition *> *condition __attribute__((swift_name("condition")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Trigger.Scroll")))
@interface ComposeAppTriggerScroll : ComposeAppTrigger
- (instancetype)initWithPercentage:(int32_t)percentage condition:(NSArray<ComposeAppCondition *> *)condition __attribute__((swift_name("init(percentage:condition:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppTriggerScroll *)doCopyPercentage:(int32_t)percentage condition:(NSArray<ComposeAppCondition *> *)condition __attribute__((swift_name("doCopy(percentage:condition:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSArray<ComposeAppCondition *> *condition __attribute__((swift_name("condition")));
@property (readonly) int32_t percentage __attribute__((swift_name("percentage")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Trigger.TimeOnPage")))
@interface ComposeAppTriggerTimeOnPage : ComposeAppTrigger
- (instancetype)initWithValue:(int32_t)value condition:(NSArray<ComposeAppCondition *> *)condition __attribute__((swift_name("init(value:condition:)"))) __attribute__((objc_designated_initializer));
- (ComposeAppTriggerTimeOnPage *)doCopyValue:(int32_t)value condition:(NSArray<ComposeAppCondition *> *)condition __attribute__((swift_name("doCopy(value:condition:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSArray<ComposeAppCondition *> *condition __attribute__((swift_name("condition")));
@property (readonly) int32_t value __attribute__((swift_name("value")));
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
- (void)showPopup:(ComposeAppPopupDefinition *)popup context:(ComposeAppPlatformContext *)context onAction:(void (^)(ComposeAppAction *))onAction onDismiss:(void (^)(void))onDismiss __attribute__((swift_name("show(popup:context:onAction:onDismiss:)")));
@end


/** Persistencia sencilla para cooldowns y timestamps multiplataforma */
__attribute__((swift_name("KeyValueStorage")))
@protocol ComposeAppKeyValueStorage
@required
- (ComposeAppLong * _Nullable)getLongKey:(NSString *)key __attribute__((swift_name("getLong(key:)")));
- (void)putLongKey:(NSString *)key value:(int64_t)value __attribute__((swift_name("putLong(key:value:)")));
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
- (void)putLongKey:(NSString *)key value:(int64_t)value __attribute__((swift_name("putLong(key:value:)")));
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

__attribute__((swift_name("KotlinIterator")))
@protocol ComposeAppKotlinIterator
@required
- (BOOL)hasNext __attribute__((swift_name("hasNext()")));
- (id _Nullable)next __attribute__((swift_name("next()")));
@end

#pragma pop_macro("_Nullable_result")
#pragma clang diagnostic pop
NS_ASSUME_NONNULL_END
