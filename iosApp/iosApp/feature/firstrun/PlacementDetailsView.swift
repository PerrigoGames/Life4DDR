//
//  PlacementDetailsView.swift
//  iosApp
//
//  Created by Andrew Le on 6/27/24.
//  Copyright © 2024 Life4DDR. All rights reserved.
//

import SwiftUI
import Shared

struct PlacementDetailsView: View {
    @ObservedObject var viewModel: PlacementDetailsViewModel
    @State var state: UIPlacementDetails?
    var placementId: String
    
    init(placementId: String) {
        self.placementId = placementId
        viewModel = PlacementDetailsViewModel(placementId: placementId)
    }
    
    var body: some View {
        VStack {
            HStack {
                if (state != nil) {
                    Image(String(describing: (state?.rankIcon)!).lowercased())
                        .resizable()
                        .frame(width: 64.0, height: 64.0)
                }
                Text(placementId.components(separatedBy: "_")[0].uppercased())
                    .font(.system(size: 36, weight: .bold))
                    .padding(.leading, 8)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(8)
            
            VStack {
                Text("This Placement Set must be done in a single set. Song order does not matter, and you can use extra stage to replay a song of your choice.")
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(8)
                ForEach(state?.songs ?? [], id: \.self) { song in
                    HStack {
                        AsyncImage(url: URL(string: song.jacketUrl!)) { image in
                            image.image?.resizable()
                        }
                        .frame(width: 86, height: 86)

                        VStack {
                            Text(song.songNameText)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .font(.system(size: 20, weight: .bold))
                                .minimumScaleFactor(0.5)
                                .lineLimit(1)
                            Text(song.subtitleText)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .font(.system(size: 16, weight: .bold))
                                .minimumScaleFactor(0.5)
                                .lineLimit(1)
                            Text("\(song.difficultyClass) \(song.difficultyText)")
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .font(.system(size: 16, weight: .bold))
                                .minimumScaleFactor(0.5)
                                .lineLimit(1)
                                .foregroundColor(Color(song.difficultyClass.colorRes.getUIColor()))
                        }
                    }.padding(16)
                }
                Text("When you've finished the set, remember to take a picture of the results screen!")
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(8)
            }
            Spacer()
            Button {
                withAnimation {

                }
            } label: {
                Text("SUBMIT PHOTO")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color(.accent))
                    .foregroundColor(.white)
                    .font(.system(size: 24, weight: .medium))
                    .cornerRadius(10)
            }
        }
        .navigationBarTitleDisplayMode(.inline)
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
    PlacementDetailsView(placementId: "copper_placement")
}
