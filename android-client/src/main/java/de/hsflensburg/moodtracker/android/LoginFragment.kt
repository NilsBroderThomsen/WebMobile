package de.hsflensburg.moodtracker.android

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2

class LoginFragment : Fragment(R.layout.fragment_login) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.loginRegisterButton).setOnClickListener {
            activity?.findViewById<ViewPager2>(R.id.authPager)?.currentItem = 1
        }
    }
}
