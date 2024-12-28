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
                ForEach(data!.goals.rawGoals, id: \.self) { goal in
                    LadderGoalItem(
                        goal: goal,
                        allowCompleting: data!.allowCompleting,
                        allowHiding: data!.allowHiding,
                        onCompletedChanged: onCompletedChanged,
                        onHiddenChanged: onHiddenChanged
                    )
                }
            } else if data?.goals is UILadderGoals.CategorizedList {
                Text("FIXME")
            }
        }
    }
}

struct LadderGoalItem: View {
    var goal: UILadderGoal
    var allowCompleting: Bool = true
    var allowHiding: Bool = true
    var onCompletedChanged: (Int64) -> (Void)
    var onHiddenChanged: (Int64) -> (Void)
    
    var body: some View {
        VStack {
            LadderGoalHeaderRow(
                goal: goal,
                allowCompleting: allowCompleting,
                allowHiding: allowHiding,
                onCompletedChanged: onCompletedChanged,
                onHiddenChanged: onHiddenChanged
            )
            // TODO: add goal details and progress
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
            // TODO: add progress text
            if allowCompleting {
                Button {
                    withAnimation {
                        onCompletedChanged(goal.id)
                    }
                } label: {
                    Image(systemName: goal.completed ? "checkmark.square.fill" : "square")
                }.buttonStyle(.plain)
            }
            if allowHiding {
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
