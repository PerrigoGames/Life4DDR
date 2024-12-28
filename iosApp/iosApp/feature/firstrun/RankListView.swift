//
//  RankListView.swift
//  iosApp
//
//  Created by Andrew Le on 2/26/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import Shared

struct RankListView: View {
    var isFirstRun: Bool
    @ObservedObject var viewModel: RankListViewModel
    @State var state: UIRankList?
    var onAction: (RankListViewModel.Action) -> (Void)
    
    init(isFirstRun: Bool = false, onAction: @escaping (RankListViewModel.Action) -> (Void) = { _ in }) {
        self.isFirstRun = isFirstRun
        viewModel = RankListViewModel(isFirstRun: isFirstRun)
        self.onAction = onAction
    }
    
    var body: some View {
        VStack {
            if (state != nil) {
                RankSelection(
                    data: state!,
                    onInput: { input in
                        viewModel.onInputAction(input: input)
                    }
                )
            }
            if (state?.ladderData != nil) {
                LadderGoals(
                    data: state?.ladderData,
                    onCompletedChanged: {_ in },
                    onHiddenChanged: {_ in }
                )
            } else {
                Spacer()
            }
            if (state?.footer != nil) {
                FirstRunWidget(
                    data: (state?.footer)!,
                    onInput: { input in
                        viewModel.onInputAction(input: input)
                    }
                )
            }
        }
        .navigationBarBackButtonHidden(!(state?.showBackButton ?? false))
        .navigationTitle(state?.titleText.desc().localized() ?? "")
        .navigationBarTitleDisplayMode(.large)
        .onAppear {
            viewModel.state.subscribe { state in
                if let currentState = state {
                    withAnimation {
                        self.state = currentState
                    }
                }
            }
            viewModel.actions.subscribe { action in
                onAction(action!)
            }
        }
    }
}

struct FirstRunWidget: View {
    var data: UIFooterData
    var onInput: (RankListViewModel.Input) -> (Void)
    
    var body: some View {
        Text(data.footerText?.localized() ?? "")
            .minimumScaleFactor(0.5)
            .lineLimit(1)
        Button {
            withAnimation {
                onInput(data.buttonInput)
            }
        } label: {
            Text(data.buttonText.localized())
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color(red: 1, green: 0, blue: 0.44))
                .foregroundColor(.white)
                .clipShape(Capsule())
                .font(.system(size: 16, weight: .medium))
        }
    }
}

#Preview {
    RankListView()
}
