package com.project.kioasktab.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.project.kioasktab.Fragment.MenuOneFragment
import com.project.kioasktab.Fragment.MenuThreeFragment
import com.project.kioasktab.Fragment.MenuTwoFragment

class ViewPageAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    val fragment: List<Fragment>

    init {
        fragment = listOf(MenuOneFragment(), MenuTwoFragment(), MenuThreeFragment())
    }

    override fun getItemCount(): Int = fragment.size

    override fun createFragment(position: Int): Fragment = fragment[position]
}