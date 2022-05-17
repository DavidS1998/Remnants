package com.falls.remnants.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter


// ViewPager class for the tab fragments
class AdapterTabPager(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private val mFragmentList: MutableList<Fragment> = ArrayList()
    //        private val mFragmentTitleList: MutableList<String> = ArrayList()
    private val hashMap: HashMap<Int, Fragment> = HashMap()


    fun addFragment(fragment: Fragment) {
        mFragmentList.add(fragment)
//            mFragmentTitleList.add(title)
    }

    override fun getItemCount(): Int {
        return mFragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        val fragment: Fragment = mFragmentList[position]
        hashMap[position] = fragment
        return fragment
    }
}