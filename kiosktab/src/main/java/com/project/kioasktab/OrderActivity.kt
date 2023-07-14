package com.project.kioasktab

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.widget.ViewPager2
import com.project.kioasktab.Adapter.ViewPageAdapter
import com.project.kioasktab.Fragment.CartFragment
import com.project.kioasktab.databinding.ActivityOrderBinding

class OrderActivity : AppCompatActivity() {

    lateinit var binding: ActivityOrderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderBinding.inflate(layoutInflater)

        setContentView(binding.root)
        var red = ContextCompat.getColor(this, R.color.red)
        var black = ContextCompat.getColor(this, R.color.black)
        var light_blue = ContextCompat.getColor(this, R.color.light_blue_900)
        var white = ContextCompat.getColor(this, R.color.white)


        binding.orderViewPager.adapter = ViewPageAdapter(this)
        binding.orderViewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        pageChangeCallback()
//
//        Handler().postDelayed({
//            intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//        },90000)

        binding.orderMain.setOnClickListener {
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.Menu1.setOnClickListener {
            binding.orderViewPager.visibility = View.VISIBLE

            binding.orderViewPager.setCurrentItem(0, true)
        }

        binding.Menu2.setOnClickListener {
            binding.orderViewPager.visibility = View.VISIBLE

            binding.orderViewPager.setCurrentItem(1, true)
        }

        binding.Menu3.setOnClickListener {
            binding.orderViewPager.visibility = View.VISIBLE

            binding.orderViewPager.setCurrentItem(2, true)
        }

        binding.Cart.setOnClickListener {
            binding.orderViewPager.visibility = View.GONE
            val fragmentManager: FragmentManager = supportFragmentManager
            val transaction: FragmentTransaction = fragmentManager.beginTransaction()

            var cartFragment = CartFragment()
            transaction.replace(R.id.order_frame, cartFragment)
            transaction.commit()

            binding.fragMenu1.visibility = View.GONE
            binding.fragMenu2.visibility = View.GONE
            binding.fragMenu3.visibility = View.GONE
            binding.fragCart.visibility = View.VISIBLE

            binding.Menu1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f)
            binding.Menu2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f)
            binding.Menu3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f)
            binding.Cart.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35f)

            binding.Menu1.setTextColor(black)
            binding.Menu2.setTextColor(black)
            binding.Menu3.setTextColor(black)
            binding.Cart.setTextColor(red)

            CartFragment()
        }
    }

    fun pageChangeCallback() {
        binding.orderViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                var red = ContextCompat.getColor(this@OrderActivity, R.color.red)
                var black = ContextCompat.getColor(this@OrderActivity, R.color.black)
                var light_blue = ContextCompat.getColor(this@OrderActivity, R.color.light_blue_900)
                var white = ContextCompat.getColor(this@OrderActivity, R.color.white)

                binding.apply {
                    fragMenu1.visibility = View.GONE
                    fragMenu2.visibility = View.GONE
                    fragMenu3.visibility = View.GONE
                    fragCart.visibility = View.GONE

                    binding.Menu1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f)
                    binding.Menu2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f)
                    binding.Menu3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f)
                    binding.Cart.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f)

                    binding.Menu1.setTextColor(black)
                    binding.Menu2.setTextColor(black)
                    binding.Menu3.setTextColor(black)
                    binding.Cart.setTextColor(black)
                }

                when (position) {
                    0 -> {
                        binding.fragMenu1.visibility = View.VISIBLE
                        binding.Menu1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35f)
                        binding.Menu1.setTextColor(red)
                    }

                    1 -> {
                        binding.fragMenu2.visibility = View.VISIBLE
                        binding.Menu2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35f)
                        binding.Menu2.setTextColor(red)
                    }

                    2 -> {
                        binding.fragMenu3.visibility = View.VISIBLE
                        binding.Menu3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35f)
                        binding.Menu3.setTextColor(red)
                    }

                    3 -> {
                        binding.fragCart.visibility = View.VISIBLE
                        binding.Cart.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35f)
                        binding.Cart.setTextColor(red)
                    }
                }
            }
        })
    }
}

