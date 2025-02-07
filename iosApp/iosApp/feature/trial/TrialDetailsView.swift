//
//  TrialDetailsView.swift
//  iosApp
//
//  Created by Andrew Le on 6/26/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import Shared

@available(iOS 16.0, *)
struct TrialDetailsView: View {
    var trial: Trial
    
    var body: some View {
        ZStack {
            VStack {
                let imageResource = trial.coverResource as? ImageDescResource
                Image(uiImage: (imageResource != nil ? imageResource?.resource.toUIImage() : MR.images().trial_default.toUIImage())!)
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(minWidth: 0, maxWidth: .infinity)
                    .opacity(0.25)
                Spacer()
            }
            
            VStack {
                HStack {
                    Text(trial.name.uppercased())
                        .font(.system(size: 36, weight: .heavy))
                    Spacer()
                    Text("LV \(String(Int(truncating: trial.difficulty ?? 0)))")
                        .font(.system(size: 24, weight: .medium))
                }
                HStack {
                    Text("EX")
                        .font(.system(size: 24, weight: .heavy))
                    ProgressView(value: Double(trial.totalEx), total: Double(trial.totalEx)).tint(Color(.accent))
                        .scaleEffect(x: 1, y: 4, anchor: .center)
                    Text("\(String(trial.totalEx))/\(String(trial.totalEx))")
                }
                // TODO: goal list should be provided by view model
                VStack(alignment: .leading) {
                    HStack {
                        Image("onyx5")
                            .resizable()
                            .frame(width: CGFloat(48.0), height: CGFloat(48.0))
                        Text(trial.goals?.last?.rank.nameRes.desc().localized() ?? "")
                            .font(.system(size: 24, weight: .heavy))
                    }
                    HStack {
                        Image(systemName: "checkmark")
                            .font(.system(size: 18, weight: .heavy))
                        Text("\(String(Int(truncating: trial.goals?.last?.exMissing ?? 0))) missing EX or less (\(String(Int(trial.totalEx) - Int(truncating: trial.goals?.last?.exMissing ?? 0))) EX)")
                    }
                }.frame(
                    minWidth: 0,
                    maxWidth: .infinity,
                    alignment: .topLeading
                )
                Spacer()
                LazyVGrid(columns: [GridItem(.adaptive(minimum: 180))], spacing: 8) {
                    ForEach(trial.songs, id: \.self) { song in
                        VStack {
                            AsyncImage(url: URL(string: song.url!)) { image in
                                image.image?.resizable()
                            }
                            .frame(width: 175, height: 175)
                            // TODO: song data needs to be provided by view model, lacks chart difficulty
                            Text(song.difficultyClass.name).padding(.leading, 10).font(.system(size: 18, weight: .heavy)).foregroundColor(Color(song.difficultyClass.colorRes.getUIColor()))
                        }
                    }
                }.gridCellColumns(2)
                Button {
                    withAnimation {

                    }
                } label: {
                    Text("START")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color(.accent))
                        .foregroundColor(.white)
                        .font(.system(size: 24, weight: .medium))
                        .cornerRadius(10)
                }
            }
        }        
        .navigationBarTitleDisplayMode(.inline)
    }
}

//#Preview {
//    TrialDetailsView()
//}
