// swift-tools-version: 5.7
import PackageDescription

let package = Package(
    name: "DeepdotsSDK",
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(name: "ComposeApp", targets: ["ComposeApp"]) // módulo consumido desde Swift
    ],
    targets: [
        .binaryTarget(
            name: "ComposeApp",
            url: "https://github.com/MagicFeedback/DeepdotsSDK-SPM/releases/download/0.1.3/DeepdotsSDK-0.1.3.xcframework.zip",
            checksum: "24a0e6faf81a496667d98d46f7b94444fabd3b9f711bbd8d2dbbccb9d07ad985"
        )
    ]
)
