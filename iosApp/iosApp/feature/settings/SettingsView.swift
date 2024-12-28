//
//  SettingsView.swift
//  iosApp
//
//  Created by Andrew Le on 6/25/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import Shared

struct SettingsView: View {
    @ObservedObject var viewModel: SettingsViewModel = SettingsViewModel(onClose: {}, onNavigateToCredits: {})
    @State var state: UISettingsData?
    
    var body: some View {
        VStack {
            SettingsViewContent(
                items: state?.settingsItems,
                onAction: { action in
                    viewModel.handleAction(action: action)
                }
            )
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

struct SettingsViewContent: View {
    var items: [UISettingsItem]?
    var onAction: (SettingsAction) -> (Void)
    
    var body: some View {
        List {
            Section {
                ForEach(items?[0...3] ?? [], id: \.self) { item in
                    var link = item as? UISettingsItem.Link
                    Button {
//                        withAnimation {
//                            onAction(link!.action)
//                        }
                    } label: {
                        Text(link!.title.localized())
                    }.buttonStyle(.plain)
                    
                }
            }
            Section {
                ForEach(items?[6...] ?? [], id: \.self) { item in
                    let link = item as? UISettingsItem.Link
                    Button {
//                        withAnimation {
//                            onAction(link!.action)
//                        }
                    } label: {
                        VStack(alignment: .leading) {
                            Text(link?.title.localized() ?? "")
                            if (link?.subtitle != nil) {
                                Text(link?.subtitle?.localized() ?? "")
                                    .font(.system(size: 13, weight: .bold))
                            }
                        }
                    }.buttonStyle(.plain)
                }
            } header: {
                Text((items?[5] as? UISettingsItem.Header)?.title.localized() ?? "")
            }
        }
    }
}

#Preview {
    SettingsView()
}
