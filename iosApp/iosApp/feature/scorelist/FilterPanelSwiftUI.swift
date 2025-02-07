//
//  FilterPanelSwiftUI.swift
//  iosApp
//
//  Created by Andrew Le on 1/28/25.
//  Copyright © 2025 orgName. All rights reserved.
//

import SwiftUI
import Shared

@available(iOS 16.0, *)
struct FilterPane: View {
    var data: UIFilterView
    var onAction: (UIFilterAction) -> (Void)
    @Environment(\.colorScheme) var colorScheme
    
    var body: some View {
        VStack {
            // Play Style Selection (Single/Double)
            // TODO: figure out how to style the control group buttons
            if (data.playStyleSelector != nil) {
                ControlGroup {
                    ForEach(data.playStyleSelector!, id: \.self) { item in
                        Button {
                            withAnimation {
                                onAction(item.action)
                            }
                        } label: {
                            Text(item.text.localized())
                        }
                    }
                }
            }
            
            // Difficulty Toggles (e.g. bSP/BSP/DSP/ESP/CSP)
            HStack {
                ForEach(data.difficultyClassSelector, id: \.self) { item in
                    VStack {
                        Toggle("", isOn: Binding(
                            get: { item.selected },
                            set: { _ in
                                onAction(item.action)
                            }
                        ))
                        .frame(maxWidth: .infinity)
                        .labelsHidden()
                        Text(item.text.localized())
                    }
                }
            }
            
            Spacer().frame(height: 8)
            
            // Difficulty Slider (1-19)
            Text(data.difficultyNumberTitle.localized()).frame(maxWidth: .infinity, alignment: .leading)
            RangeSliderView(
                value: Binding(
                    get: { ((data.difficultyNumberRange.innerFloatRange.start as? CGFloat)!)...((data.difficultyNumberRange.innerFloatRange.endInclusive as? CGFloat)!) },
                    set: { range in
                        onAction(UIFilterAction.SetDifficultyNumberRange(min: Int32(range.lowerBound.rounded()), max: Int32(range.upperBound.rounded())))
                    }
                ),
                range: ((data.difficultyNumberRange.outerFloatRange.start as? CGFloat)!)...((data.difficultyNumberRange.outerFloatRange.endInclusive as? CGFloat)!),
                minimumDistance: 0
            )
            HStack {
                Text(String(describing: data.difficultyNumberRange.innerRange.start))
                Spacer()
                Text(String(describing: data.difficultyNumberRange.innerRange.endInclusive))
            }
            
            Spacer().frame(height: 8)
            
            // Clear Type Slider
            Text(data.clearTypeTitle.localized()).frame(maxWidth: .infinity, alignment: .leading)
            RangeSliderView(
                value: Binding(
                    get: { ((data.clearTypeRange.innerFloatRange.start as? CGFloat)!)...((data.clearTypeRange.innerFloatRange.endInclusive as? CGFloat)!) },
                    set: { range in
                        onAction(UIFilterAction.SetClearTypeRange(min: Int32(range.lowerBound.rounded()), max: Int32(range.upperBound.rounded())))
                    }
                ),
                range: ((data.clearTypeRange.outerFloatRange.start as? CGFloat)!)...((data.clearTypeRange.outerFloatRange.endInclusive as? CGFloat)!),
                minimumDistance: 0
            )
            HStack {
                Text(String(ClearType.entries[(data.clearTypeRange.innerRange.start as? Int)!].uiName.localized()))
                Spacer()
                Text(String(ClearType.entries[(data.clearTypeRange.innerRange.endInclusive as? Int)!].uiName.localized()))
            }
            
            Spacer().frame(height: 8)
            
            // Score Range
            // TODO: (shared) make score range inputs functional, if Android version is functional
            HStack(spacing: 16) {
                TextField(data.scoreRangeBottomHint.localized(), text: Binding(
                    get: { data.scoreRangeBottomValue != nil ? String(describing: data.scoreRangeBottomValue) : "" },
                    set: { value in
                        onAction(UIFilterAction.SetScoreRange(first: Int(value) as? KotlinInt, last: data.scoreRangeTopValue))
                    })
                )
                    .disableAutocorrection(true)
                    .padding(12)
                    .overlay(
                        RoundedRectangle(cornerRadius: 5)
                            .stroke(colorScheme == .dark ? .white : .black)
                    )
                TextField(data.scoreRangeTopHint.localized(), text: Binding(
                    get: { data.scoreRangeTopValue != nil ? String(describing: data.scoreRangeTopValue) : "" },
                    set: { value in
                        onAction(UIFilterAction.SetScoreRange(first: data.scoreRangeBottomValue, last: Int(value) as? KotlinInt))
                    })
                )
                    .disableAutocorrection(true)
                    .padding(12)
                    .overlay(
                        RoundedRectangle(cornerRadius: 5)
                            .stroke(colorScheme == .dark ? .white : .black)
                    )
            }
            
        }.padding(.horizontal, 16)
    }
}

// TODO: figure out how to have the slider snap into place, and for the two ends to overlap
// TODO: figure out how to stop triggering the slider change methods as it's sliding
@available(iOS 16.0, *)
struct RangeSliderView: View {
    @Binding var value: ClosedRange<CGFloat>
    var range: ClosedRange<CGFloat>
    var minimumDistance: CGFloat = 0
    
    @State private var slider1: GestureProperties = .init()
    @State private var slider2: GestureProperties = .init()
    @State private var indicatorWidth: CGFloat = 0
    @State private var isInitial: Bool = false
    
    var body: some View {
        GeometryReader { reader in
            let maxSliderWidth = reader.size.width - 30
            let minimumDistance = minimumDistance == 0 ? 0 : (minimumDistance / (range.upperBound - range.lowerBound)) * maxSliderWidth
            
            ZStack(alignment: .leading) {
                Capsule()
                    .fill(.tertiary)
                    .frame(height: 5)
                
                // Sliders
                HStack(spacing: 0) {
                    Circle()
                        .fill(.primary)
                        .frame(width: 15, height: 15)
                        .contentShape(.rect)
                        .overlay(alignment: .leading) {
                            Rectangle()
                                .frame(width: indicatorWidth, height: 5)
                                .offset(x: 15)
                                .allowsHitTesting(/*@START_MENU_TOKEN@*/false/*@END_MENU_TOKEN@*/)
                        }
                        .offset(x: slider1.offset)
                        .gesture(
                            DragGesture(minimumDistance: 0)
                                .onChanged { value in
                                    // Calculating offset
                                    var translation = value.translation.width + slider1.lastStoredOffset
                                    translation = min(max(translation, 0), slider2.offset - minimumDistance)
                                    slider1.offset = translation
                                    calculateNewRange(reader.size)
                                }.onEnded { _ in
                                    slider1.lastStoredOffset = slider1.offset
                                }
                        )
                    
                    Circle()
                        .fill(.primary)
                        .frame(width: 15, height: 15)
                        .contentShape(.rect)
                        .offset(x: slider2.offset)
                        .gesture(
                            DragGesture(minimumDistance: 0)
                                .onChanged { value in
                                    // Calculating offset
                                    var translation = value.translation.width + slider2.lastStoredOffset
                                    translation = min(max(translation, slider1.offset + minimumDistance), maxSliderWidth)
                                    slider2.offset = translation
                                    calculateNewRange(reader.size)
                                }.onEnded { _ in
                                    slider2.lastStoredOffset = slider2.offset
                                }
                        )
                }
            }
            .frame(maxHeight: .infinity)
            .task {
                guard !isInitial else { return }
                isInitial = true
                try? await Task.sleep(for: .seconds(0))
                let maxWidth = reader.size.width - 30
                
                // Converting selection range into offset
                let start = value.lowerBound.interpolate(inputRange: [range.lowerBound, range.upperBound], outputRange: [0, maxWidth])
                let end = value.upperBound.interpolate(inputRange: [range.lowerBound, range.upperBound], outputRange: [0, maxWidth])
                
                slider1.offset = start
                slider1.lastStoredOffset = start
                slider2.offset = end
                slider2.lastStoredOffset = end
                calculateNewRange(reader.size)
            }
        }
        .frame(height: 20)
    }
    
    private func calculateNewRange(_ size: CGSize) {
        indicatorWidth = slider2.offset - slider1.offset
        
        let maxWidth = size.width - 30
        // Calculating new range values
        let startProgress = slider1.offset / maxWidth
        let endProgress = slider2.offset / maxWidth
        
        // Interpolating between upper and lower bounds
        let newRangeStart = range.lowerBound.interpolated(towards: range.upperBound, amount: startProgress)
        let newRangeEnd = range.lowerBound.interpolated(towards: range.upperBound, amount: endProgress)
        
        // Update value
        value = newRangeStart...newRangeEnd
    }
    
    private struct GestureProperties {
        var offset: CGFloat = 0
        var lastStoredOffset: CGFloat = 0
    }
}

// Interpolation
extension CGFloat {
    func interpolate(inputRange: [CGFloat], outputRange: [CGFloat]) -> CGFloat {
        // If value is less than its initial input range
        let x = self
        let length = inputRange.count - 1
        if x <= inputRange[0] { return outputRange[0] }
        
        for index in 1...length {
            let x1 = inputRange[index - 1]
            let x2 = inputRange[index]
            
            let y1 = outputRange[index - 1]
            let y2 = outputRange[index]
            
            // Linear interpolation formula: y1 + ((y2-y1) / (x2-x1)) * (x-x1)
            if x <= inputRange[index] {
                let y = y1 + ((y2-y1) / (x2-x1)) * (x-x1)
                return y
            }
        }
        
        // If value exceeds its maximum output range
        return outputRange[length]
    }
}

//#Preview {
//    FilterPanelSwiftUI()
//}
