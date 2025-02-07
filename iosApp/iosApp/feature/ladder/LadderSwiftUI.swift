//
//  LadderSwiftUI.swift
//  iosApp
//
//  Created by Andrew Le on 3/1/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import Shared

struct LadderGoals: View {
    var data: UILadderData?
    var onCompletedChanged: (Int64) -> (Void)
    var onHiddenChanged: (Int64) -> (Void)
    
    var body: some View {
        ScrollView(showsIndicators: false) {
            if data?.goals is UILadderGoals.SingleList {
                SingleGoalList(
                    goals: data!.goals as! UILadderGoals.SingleList,
                    allowCompleting: data!.allowCompleting,
                    allowHiding: data!.allowHiding,
                    onCompletedChanged: onCompletedChanged,
                    onHiddenChanged: onHiddenChanged
                )
            } else if data?.goals is UILadderGoals.CategorizedList {
                CategorizedList(
                    goals: data!.goals as! UILadderGoals.CategorizedList,
                    allowCompleting: data!.allowCompleting,
                    allowHiding: data!.allowHiding,
                    onCompletedChanged: onCompletedChanged,
                    onHiddenChanged: onHiddenChanged
                )
            }
        }
    }
}

struct SingleGoalList: View {
    var goals: UILadderGoals.SingleList
    var allowCompleting: Bool = true
    var allowHiding: Bool = true
    var onCompletedChanged: (Int64) -> (Void)
    var onHiddenChanged: (Int64) -> (Void)
    
    var body: some View {
        ForEach(goals.items, id: \.self) { goal in
            LadderGoalItem(
                goal: goal,
                allowCompleting: allowCompleting,
                allowHiding: allowHiding,
                onCompletedChanged: onCompletedChanged,
                onHiddenChanged: onHiddenChanged
            )
        }
    }
}

struct CategorizedList: View {
    var goals: UILadderGoals.CategorizedList
    var allowCompleting: Bool = true
    var allowHiding: Bool = true
    var onCompletedChanged: (Int64) -> (Void)
    var onHiddenChanged: (Int64) -> (Void)
    
    var body: some View {
        ForEach(goals.categories, id: \.self) { category in
            HStack {
                Text((category.first?.title.localized())!)
                Spacer()
                if (category.first?.goalText != nil) {
                    Text((category.first?.goalText?.localized())!)
                }
            }
            ForEach((category.second as? [UILadderGoal])!, id: \.self) { goal in
                LadderGoalItem(
                    goal: goal,
                    allowCompleting: allowCompleting,
                    allowHiding: allowHiding,
                    onCompletedChanged: onCompletedChanged,
                    onHiddenChanged: onHiddenChanged
                )
            }
            Spacer().frame(height: 16)
        }
    }
}

struct LadderGoalItem: View {
    var goal: UILadderGoal
    var allowCompleting: Bool = true
    var allowHiding: Bool = true
    var onCompletedChanged: (Int64) -> (Void)
    var onHiddenChanged: (Int64) -> (Void)
    @State var isExpanded: Bool = false
    
    var body: some View {
        VStack {
            LadderGoalHeaderRow(
                goal: goal,
                allowCompleting: allowCompleting,
                allowHiding: allowHiding,
                onCompletedChanged: onCompletedChanged,
                onHiddenChanged: onHiddenChanged
            )
            if !goal.detailItems.isEmpty {
                Divider().overlay(Color.primary)
                LadderGoalDetailShade(items: goal.detailItems)
            }
            if goal.progress != nil {
                ProgressView(value: goal.progress?.progressPercent)
                    .frame(maxWidth: .infinity)
                    .accentColor(.green)
                    .scaleEffect(x: 1, y: 4, anchor: .center)
            }
        }
        .background(Color("goalBackground"))
        .opacity(goal.hidden ? 0.5 : 1.0)
        .cornerRadius(10.0)
    }
}

struct LadderGoalHeaderRow: View {
    var goal: UILadderGoal
    var allowCompleting: Bool = true
    var allowHiding: Bool = true
    var onCompletedChanged: (Int64) -> (Void)
    var onHiddenChanged: (Int64) -> (Void)
    
    var body: some View {
        HStack {
            Text(goal.goalText.localized())
            Spacer()
            if goal.progress != nil {
                Text(goal.progress!.progressText)
            }
            if allowCompleting {
                Button {
                    withAnimation {
                        onCompletedChanged(goal.id)
                    }
                } label: {
                    Image(systemName: goal.completed ? "checkmark.square.fill" : "square")
                }.buttonStyle(.plain)
            }
            if allowHiding && goal.canHide {
                Button {
                    withAnimation {
                        onHiddenChanged(goal.id)
                    }
                } label: {
                    Image(systemName: "eye.fill")
                }.buttonStyle(.plain)
            }
        }.padding(12)
    }
}

struct LadderGoalDetailShade: View {
    var items: [UILadderDetailItem]
    
    var body: some View {
        LazyVStack(spacing: 4) {
            ForEach(items, id: \.self) { item in
                HStack {
                    Text(item.leftText)
                        .foregroundColor(Color(item.leftColor?.getUIColor() ?? .label))
                    if item.rightText != nil {
                        Spacer()
                        Text(item.rightText!)
                            .foregroundColor(Color(item.rightColor?.getUIColor() ?? .label))
                    }
                }.frame(maxWidth: .infinity)
            }
        }.padding(12)
    }
}

func createSongDetailItem(songName: String, difficultyClass: DifficultyClass? = nil) -> UILadderDetailItem {
    let score: Int = 1000000 - Int.random(in: 100...50000)
    return UILadderDetailItem(leftText: songName, difficultyClass: difficultyClass, rightText: String(score))
}

func createUILadderGoal(goalText: String, completed: Bool = false, hidden: Bool = false, progress: UILadderProgress? = nil, detailItems: [UILadderDetailItem] = []) -> UILadderGoal {
    return UILadderGoal(id: 0, goalText: RawStringDesc(string: goalText), completed: completed, hidden: hidden, canHide: true, isMandatory: false, progress: progress, detailItems: detailItems)
}

let detailItems = [
    createSongDetailItem(songName: "L'amour et la libert&eacute;(DDR Ver.)", difficultyClass: DifficultyClass.beginner),
    createSongDetailItem(songName: "LOVE&hearts;SHINE", difficultyClass: DifficultyClass.basic),
    createSongDetailItem(songName: "Miracle Moon ～L.E.D.LIGHT STYLE MIX～", difficultyClass: DifficultyClass.difficult),
    createSongDetailItem(songName: "PARANOIA survivor", difficultyClass: DifficultyClass.expert),
    createSongDetailItem(songName: "PARANOIA survivor MAX", difficultyClass: DifficultyClass.challenge),
    createSongDetailItem(songName: "Pink Rose", difficultyClass: DifficultyClass.beginner),
    createSongDetailItem(songName: "SO IN LOVE", difficultyClass: DifficultyClass.basic),
    createSongDetailItem(songName: "STAY (Organic house Version)", difficultyClass: DifficultyClass.difficult),
    createSongDetailItem(songName: "stoic (EXTREME version)", difficultyClass: DifficultyClass.expert),
    createSongDetailItem(songName: "sync (EXTREME version)", difficultyClass: DifficultyClass.challenge),
    createSongDetailItem(songName: "TEARS")
]

struct PreviewGoalItem: View {
    var goal: UILadderGoal
    
    var body: some View {
        LadderGoalItem(goal: goal, onCompletedChanged: {_ in }, onHiddenChanged: {_ in })
    }
}

struct LadderGoalItemDetail_Previews: PreviewProvider {
    static var previews: some View {
        PreviewGoalItem(goal: createUILadderGoal(goalText: "Clear any 10 L5's.", progress: UILadderProgress(count: 7, max: 10), detailItems: detailItems))
    }
}
