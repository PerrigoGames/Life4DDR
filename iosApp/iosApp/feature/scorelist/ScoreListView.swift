//
//  ScoreListView.swift
//  iosApp
//
//  Created by Andrew Le on 6/25/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import Shared
import WebKit

@available(iOS 16.0, *)
struct ScoreListView: View {
    @ObservedObject var viewModel: ScoreListViewModel = ScoreListViewModel()
    @State var state: UIScoreList?
    @State var filterShowing: Bool = false
    @State var webViewShowing: Bool = false
    
    var body: some View {
        VStack {
            Button {
                withAnimation {
                    filterShowing = !filterShowing
                }
            } label: {
                Text("Filter")
                    .frame(width: 44, height: 16)
                    .padding()
                    .background(Color(red: 1, green: 0, blue: 0.44))
                    .foregroundColor(.white)
                    .clipShape(Capsule())
                    .font(.system(size: 16, weight: .medium))
            }.frame(maxWidth: .infinity, alignment: .trailing)
            if (filterShowing) {
                FilterPane(
                    data: state!.filter,
                    onAction: { action in
                       viewModel.handleFilterAction(action: action)
                   }
                )
            }
            BannerContainer(data: state?.banner)
            ZStack(alignment: .bottomTrailing) {
                List {
                    ForEach(state?.scores ?? [], id: \.self) { score in
                        ScoreEntry(data: score)
                    }
                }.listStyle(.plain)
                Button {
                    withAnimation {
                        webViewShowing.toggle()
                    }
                } label: {
                    Image(systemName: "plus")
                        .padding()
                        .background(Color(.accent))
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }.sheet(isPresented: $webViewShowing) {
                    WebView(url: URL(string: viewModel.getSanbaiUrl())!, webViewShowing: $webViewShowing)
                }
            }
            
        }
        .onAppear {
            viewModel.state.subscribe { state in
                if let currentState = state {
                    withAnimation {
                        self.state = currentState
                    }
                }
            }
        }
    }
}

struct ScoreEntry: View {
    var data: UIScore
    
    var body: some View {
        HStack {
            VStack {
                Text(data.titleText)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .lineLimit(1)
                HStack {
                    Text(data.difficultyText.localized()).foregroundColor(Color(data.difficultyColor.getUIColor()))
                    Spacer()
                    Text(data.scoreText.localized()).foregroundColor(Color(data.scoreColor.getUIColor()))
                }
            }
            if (data.flareLevel != nil) {
                let flareInt = Int(truncating: data.flareLevel!)
                
                Image(uiImage: (flareImageResource[flareInt]?.toUIImage())!)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 32.0, height: 32.0)
            } else {
                Spacer().frame(width: 40.0)
            }
        }
    }
}

var flareImageResource = [
    1: MR.images().flare_1,
    2: MR.images().flare_2,
    3: MR.images().flare_3,
    4: MR.images().flare_4,
    5: MR.images().flare_5,
    6: MR.images().flare_6,
    7: MR.images().flare_7,
    8: MR.images().flare_8,
    9: MR.images().flare_9,
    10: MR.images().flare_ex
]

@available(iOS 16.0, *)
#Preview {
    ScoreListView()
}

struct WebView: UIViewRepresentable {
    var url: URL
    @Binding var webViewShowing: Bool
    
    func makeUIView(context: Context) -> WKWebView {
        let wKWebView = WKWebView()
        wKWebView.navigationDelegate = context.coordinator
        return wKWebView
    }
    
    func updateUIView(_ uiView: WKWebView, context: Context) {
        let request = URLRequest(url: url)
        uiView.load(request)
    }
    
    func makeCoordinator() -> WebViewCoordinator {
        WebViewCoordinator(self)
    }
        
    class WebViewCoordinator: NSObject, WKNavigationDelegate {
        var parent: WebView
        var deepLinkManager = DeeplinkManager()
        
        init(_ parent: WebView) {
            self.parent = parent
        }
        
        func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
            let urlStart = "life4://sanbai_auth"
            if let urlStr = navigationAction.request.url?.absoluteString, urlStr.hasPrefix(urlStart) {
                deepLinkManager.processDeeplink(deeplink: urlStr)
                parent.webViewShowing = false
            }
            decisionHandler(.allow)
        }
    }
}
