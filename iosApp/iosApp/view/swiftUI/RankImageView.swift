//
//  RankImageView.swift
//  iosApp
//
//  Created by Andrew Le on 6/23/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import Shared

struct RankImageWithTitle: View {
    var rank: LadderRank?
    var text: String
    var imageSize: CGFloat
    var onClick: () -> (Void)
    
    init(rank: LadderRank?, text: String? = nil, imageSize: CGFloat = 84.0, onClick: @escaping () -> Void = {}) {
        self.rank = rank
        self.text = text ?? rank?.nameRes.desc().localized() ?? MR.strings().no_rank.desc().localized()
        self.imageSize = imageSize
        self.onClick = onClick
    }
    
    var body: some View {
        VStack {
            RankImage(rank: rank, size: imageSize, onClick: onClick)
            RankText(rank: rank, text: text, textWidth: imageSize + 20.0)
        }.onTapGesture { onClick() }
    }
}

struct RankText: View {
    var rank: LadderRank?
    var text: String
    var textWidth: CGFloat
    
    init(rank: LadderRank?, text: String? = nil, textWidth: CGFloat) {
        self.rank = rank
        self.text = text ?? rank?.nameRes.desc().localized() ?? MR.strings().no_rank.desc().localized()
        self.textWidth = textWidth
    }
    
    var body: some View {
        Text(text)
            .font(.system(size: 20, weight: .heavy))
            .frame(width: textWidth)
            .minimumScaleFactor(0.5)
            .lineLimit(1)
    }
}

struct RankImage: View {
    var rank: LadderRank?
    var size: CGFloat = 84.0
    var onClick: () -> (Void) = {}
    
    var body: some View {
        Image(rank != nil ? String(describing: rank!).lowercased() : "copper1")
            .resizable()
            .frame(width: size, height: size)
            .saturation(rank != nil ? 1.0 : 0.0)
            .onTapGesture { onClick() }
    }
}
