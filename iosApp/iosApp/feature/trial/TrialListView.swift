//
//  TrialListView.swift
//  iosApp
//
//  Created by Andrew Le on 6/20/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import Shared

struct TrialListView: View {
    @ObservedObject var viewModel: TrialListViewModel = TrialListViewModel()
    @State var state: UITrialList?
    
    var onTrialSelected: (Trial) -> (Void)
    var onPlacementsSelected: () -> (Void)
    
    var body: some View {
        VStack {
            if (state?.placementBanner != nil) {
                PlacementBanner(
                    banner: (state?.placementBanner)!,
                    onPlacementsSelected: onPlacementsSelected
                )
            }
            ScrollView(showsIndicators: false) {
                LazyVGrid(columns: [GridItem(.adaptive(minimum: 180))], spacing: 8) {
                    let trials = state?.trials.compactMap { $0 as? UITrialList.ItemTrial } ?? []
                    Section(header:
                        Text("Active Trials")
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .font(.system(size: 16, weight: .heavy))
                    ) {
                        ForEach(trials.filter({ !$0.data.trial.isRetired }), id: \.self) { trial in
                            TrialJacket(
                                trialData: trial.data,
                                onClick: { onTrialSelected(trial.data.trial) }
                            )
                        }
                    }
                    
                    Section(header:
                        Text("Retired Trials")
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .font(.system(size: 16, weight: .heavy))
                    ) {
                        ForEach(trials.filter({ $0.data.trial.isRetired }), id: \.self) { trial in
                            TrialJacket(
                                trialData: trial.data,
                                onClick: { onTrialSelected(trial.data.trial) }
                            )
                        }
                    }
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

struct PlacementBanner: View {
    var banner: UIPlacementBanner
    var onPlacementsSelected: () -> (Void) = {}
    
    var body: some View {
        Button {
            onPlacementsSelected()
        } label: {
            HStack(spacing: 0) {
                Text(banner.text.localized())
                    .font(.system(size: 22, weight: .heavy))
                    .padding(.trailing, 12)
                    .foregroundColor(.white)
                ForEach(banner.ranks, id: \.self) { rank in
                    Image(String(describing: rank).lowercased())
                        .resizable()
                        .frame(width: 24.0, height: 24.0)
                }
            }
        }
        .padding()
        .background(.gray).cornerRadius(10.0)
    }
}

struct TrialJacket: View {
    var trialData: UITrialJacket
    var onClick: () -> (Void)
    
    var body: some View {
        let imageResource = trialData.trial.coverResource as? ImageDescResource
        Image(uiImage: (imageResource != nil ? imageResource?.resource.toUIImage() : MR.images().trial_default.toUIImage())!)
            .resizable()
            .aspectRatio(contentMode: .fill)
            .overlay(alignment: .topLeading) {
                TrialDifficulty(difficulty: trialData.trial.difficulty as! Int)
            }
            .onTapGesture { onClick() }
    }
}

struct TrialDifficulty: View {
    var difficulty: Int
    
    var body: some View {
        Text(String(difficulty))
            .font(.system(size: 24, weight: .bold))
            .foregroundColor(.white)
            .background(
                Circle()
                    .fill(Color.blue)
                    .frame(width: 40, height: 40)
                    .opacity(/*@START_MENU_TOKEN@*/0.8/*@END_MENU_TOKEN@*/)
            )
            .padding(8)
    }
}

#Preview {
    TrialListView(onTrialSelected: { _ in }, onPlacementsSelected: {})
}
