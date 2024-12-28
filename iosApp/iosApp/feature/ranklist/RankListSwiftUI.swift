//
//  RankListSwiftUI.swift
//  iosApp
//
//  Created by Andrew Le on 2/27/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import Shared

struct RankSelection: View {    
    @Environment(\.colorScheme) var colorScheme
    var data: UIRankList
    var onInput: (RankListViewModel.Input) -> (Void)
    
    var body: some View {
        VStack {
            ScrollView(.horizontal, showsIndicators: false) {
                LazyHStack {
                    ForEach(data.rankClasses, id: \.self) { category in
                        RankImageWithTitle(
                            rank: category.rankClass?.toLadderRank(),
                            text: category.text.localized(),
                            imageSize: 64,
                            onClick: { onInput(category.tapInput) }
                        )
                        .padding(.horizontal, 8)
                    }
                }
            }.frame(height: 100)
            Divider().overlay(colorScheme == .dark ? .white : .black)
            if (data.showRankSelector) {
                RankDetailSelector(
                    availableRanks: data.ranks,
                    compress: data.isRankSelectorCompressed,
                    noRank: data.noRankInfo,
                    onInput: onInput
                )
            }
        }
    }
}

struct RankDetailSelector: View {
    var availableRanks: [UILadderRank]
    var compress: Bool
    var noRank: UINoRank
    var onInput: (RankListViewModel.Input) -> (Void)
    
    var body: some View {
        if (availableRanks.count < 5) {
            NoRankDetails(
                noRank: noRank,
                onInput: onInput
            )
        } else {
            RankCategorySelector(
                availableRanks: availableRanks,
                compressed: compress,
                onInput: onInput
            )
        }
    }
}

struct NoRankDetails: View {
    var noRank: UINoRank
    var onInput: (RankListViewModel.Input) -> (Void)
    
    var body: some View {
        VStack {
            Text(noRank.bodyText.desc().localized())
                .padding(.bottom, 16)
            Button {
                withAnimation {
                    onInput(noRank.buttonInput)
                }
            } label: {
                Text(noRank.buttonText.desc().localized())
                    .padding()
                    .background(Color(.accent))
                    .foregroundColor(.white)
                    .clipShape(Capsule())
                    .font(.system(size: 16, weight: .medium))
            }
        }
        .padding(EdgeInsets(top: 16, leading: 24, bottom: 16, trailing: 24))
        .transition(.move(edge: .top).combined(with: .opacity))
    }
}

struct RankCategorySelector: View {
    var availableRanks: [UILadderRank]
    var compressed: Bool
    var onInput: (RankListViewModel.Input) -> (Void)
    
    var body: some View {
        if compressed {
            HStack {
                ForEach(availableRanks, id: \.self) { rank in
                    RankImageWithTitle(
                        rank: rank.rank,
                        text: rank.text.localized(),
                        imageSize: 48,
                        onClick: { onInput(rank.tapInput) }
                    )
                }
            }.transition(.move(edge: .top).combined(with: .opacity))
        } else {
            VStack {
                HStack {
                    ForEach(availableRanks[0..<3], id: \.self) { rank in
                        RankImageWithTitle(rank: rank.rank, onClick: { onInput(rank.tapInput) })
                            .frame(maxWidth: .infinity)
                            .transition(.move(edge: .top).combined(with: .opacity))
                    }
                }
                Spacer().frame(height: 16)
                HStack {
                    ForEach(availableRanks[3..<5], id: \.self) { rank in
                        RankImageWithTitle(rank: rank.rank, onClick: { onInput(rank.tapInput) })
                            .frame(maxWidth: .infinity)
                            .transition(.move(edge: .top).combined(with: .opacity))
                    }
                }
            }.transition(.move(edge: .top).combined(with: .opacity))
        }
    }
}
