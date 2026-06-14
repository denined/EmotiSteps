package com.example.emotiontherapy.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class ChildrenViewModel(application: Application) : AndroidViewModel(application) {
    private val store = EmotionTherapyStoreProvider.get(application)

    fun selectChild(childId: Int) = store.selectChild(childId)
    fun clearSelectedChild() = store.clearSelectedChild()
    fun addChild(name: String) = store.addChild(name)
    fun renameChild(childId: Int, newName: String) = store.renameChild(childId, newName)
    fun deleteChild(childId: Int) = store.deleteChild(childId)
}
