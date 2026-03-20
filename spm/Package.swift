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
            url: "https://github.com/MagicFeedback/DeepdotsSDK-SPM/releases/download/0.1.7/DeepdotsSDK-0.1.7.xcframework.zip",
            checksum: "af9ae345d37befece4fed2947fb46a1f4cac8b877f75ad4d92eb29fe78b50be2"
        )
    ]
)
