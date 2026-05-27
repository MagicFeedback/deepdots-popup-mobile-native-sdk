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
            url: "https://github.com/MagicFeedback/DeepdotsSDK-SPM/releases/download/0.2.0/DeepdotsSDK-0.2.0.xcframework.zip",
            checksum: "af1f500fa607e0a086761c0e70d6ffa233572381a930fb4336e787c36d255ce5"
        )
    ]
)
