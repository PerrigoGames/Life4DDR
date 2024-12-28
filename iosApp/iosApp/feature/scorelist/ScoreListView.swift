//
//  ScoreListView.swift
//  iosApp
//
//  Created by Andrew Le on 6/25/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import Shared

struct ScoreListView: View {
    @ObservedObject var viewModel: ScoreListViewModel = ScoreListViewModel()
    @State var state: UIScoreList?
    
    var body: some View {
        VStack {
            List {
                ForEach(state?.scores ?? [], id: \.self) { score in
                    HStack {
                        Text(score.leftText)
                            .foregroundColor(Color(score.leftColor.getUIColor()))
                        Spacer()
                        Text(score.rightText)
                            .foregroundColor(Color(score.rightColor.getUIColor()))
                    }
                }
            }.listStyle(.plain)
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

#Preview {
    ScoreListView()
}
