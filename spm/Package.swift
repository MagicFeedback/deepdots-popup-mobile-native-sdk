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
            url: "https://github.com/MagicFeedback/DeepdotsSDK-SPM/releases/download/0.2.2/DeepdotsSDK-0.2.2.xcframework.zip",
            checksum: "cebee8ae4921d00b8dbec8fe21eba577cdbe3a481bfaf54c678d8327f1776db5"
        )
    ]
)
