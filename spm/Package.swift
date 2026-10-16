// swift-tools-version: 5.7
import PackageDescription

let package = Package(
    name: "DeepdotsSDK",
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(name: "DeepdotsSDK", targets: ["DeepdotsSDK"]) // módulo consumido desde Swift
    ],
    targets: [
        .binaryTarget(
            name: "DeepdotsSDK",
            url: "https://github.com/MagicFeedback/DeepdotsSDK-SPM/releases/download/0.6.0/DeepdotsSDK-0.6.0.xcframework.zip",
            checksum: "5668aae57248ee63eda0b9cace67d77efd3d0b26dba6fed959cdea245d2dd84e"
        )
    ]
)
