//
//  PlayerProfileView.swift
//  iosApp
//
//  Created by Andrew Le on 6/22/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import Shared

struct PlayerProfileView: View {
    @ObservedObject var viewModel: PlayerProfileViewModel = PlayerProfileViewModel()
    @State var playerInfoViewState: PlayerInfoViewState?
    @State var goalListViewState: ViewState<UILadderData, NSString>?
    @State var goalData: UILadderData?
    @State var goalError: String?
    var onAction: (PlayerProfileAction) -> (Void)
    
    var body: some View {
        VStack {
            PlayerProfileInfo(
                state: playerInfoViewState ?? PlayerInfoViewState(username: "", rivalCode: "", socialNetworks: [:], rank: nil, banner: nil),
                onRankClicked: { onAction(PlayerProfileAction.ChangeRank()) }
            )
            BannerContainer(data: playerInfoViewState?.banner)
            if (goalData != nil) {
                LadderGoals(
                    data: goalData,
                    onCompletedChanged: { id in
                        viewModel.goalListViewModel.handleAction(action: RankListAction.OnGoalToggleComplete(id: id))
                    },
                    onHiddenChanged: { id in
                        viewModel.goalListViewModel.handleAction(action: RankListAction.OnGoalToggleHidden(id: id))
                    }
                )
            }
            if (goalError != nil) {
                Text(goalError!).font(.system(size: 22, weight: .bold))
                Spacer()
            }
        }
        .onAppear {
            viewModel.playerInfoViewState.subscribe { state in
                if let currentState = state {
                    withAnimation {
                        self.playerInfoViewState = currentState
                    }
                }
            }
            viewModel.goalListViewModel.state.subscribe { state in
                if let currentGoalState = state {
                    goalListViewState = currentGoalState
                    goalData = (goalListViewState as? ViewStateSuccess<UILadderData>)?.data
                    let error = goalListViewState as? ViewStateError<NSString>
                    goalError = error?.error as? String
                }
            }
        }
    }
}

struct PlayerProfileInfo: View {
    var state: PlayerInfoViewState
    var onRankClicked: () -> (Void) = {}
    
    var body: some View {
        HStack {
            VStack(alignment: .leading) {
                Text(state.username)
                    .font(.system(size: 28, weight: .bold))
                Text(state.rivalCode ?? "")
                    .font(.system(size: 14, weight: .bold))
            }
            Spacer()
            RankImage(rank: state.rank, size: 64, onClick: onRankClicked)
        }
        .frame(maxWidth: .infinity)
        .padding()
    }
}

#Preview {
    PlayerProfileView(playerInfoViewState: PlayerInfoViewState(username: "Andeh", rivalCode: "6164-4734", socialNetworks: [:], rank: nil, banner: nil), onAction: { _ in })
}
