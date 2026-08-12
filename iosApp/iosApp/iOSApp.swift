import SwiftUI
import ComposeApp

@main
struct iOSApp: App {

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    if url.scheme?.lowercased() == "pixiv" {
                        PixivDeepLinkKt.handlePixivDeepLink(url: url.absoluteString)
                    } else {
                        iOSAppHelper.shared.handleIncomingImage(url: url)
                    }
                }
        }
    }
}
