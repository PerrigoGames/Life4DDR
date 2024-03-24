//
//  iosDataReader.swift
//  iosApp
//
//  Created by Andrew Le on 3/11/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import Shared

@available(iOS 16.0, *)
class iosUncachedDataReader: LocalUncachedDataReader {
    var fileName: String
    
    init(fileName: String) {
        self.fileName = fileName
    }
    
    func loadInternalString() -> String {
        return readFromFile(path: fileName) ?? "test"
    }
}

@available(iOS 16.0, *)
class iosDataReader: LocalDataReader {
    var cachedFileName: String
    
    init(cachedFileName: String) {
        self.cachedFileName = cachedFileName
    }
    
    func loadCachedString() -> String? {
        return readFromFile(path: cachedFileName)
    }
    
    func saveCachedString(data: String) -> Bool {
        return saveToFile(path: cachedFileName, content: data)
    }
    
    func deleteCachedString() -> Bool {
        let url = URL.documentsDirectory.appending(path: cachedFileName)
        do {
            try FileManager.default.removeItem(at: url)
            return true
        } catch {
            print(error.localizedDescription)
            return false
        }
    }
    
    func loadInternalString() -> String {
        return readFromFile(path: cachedFileName) ?? "test"
    }
}

@available(iOS 16.0, *)
func readFromFile(path: String) -> String? {
    let url = URL.documentsDirectory.appending(path: path)
    var ret: String?
    do {
        let data = try Data(contentsOf: url)
        ret = String(data: data, encoding: .utf8)
    } catch {
        print(error.localizedDescription)
    }
    return ret
}

@available(iOS 16.0, *)
func saveToFile(path: String, content: String) -> Bool {
    let data = Data(content.utf8)
    let url = URL.documentsDirectory.appending(path: path)
    do {
        try data.write(to: url, options: [.atomic, .completeFileProtection])
        return true
    } catch {
        print(error.localizedDescription)
        return false
    }
}