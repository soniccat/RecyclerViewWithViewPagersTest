package com.sample.viewpagertest.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.sample.viewpagertest.R

class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: GridLayoutManager
    private lateinit var adapter: TestAdapter

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
                ViewModelProvider(this).get(DashboardViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        recyclerView = root.findViewById(R.id.recycler)

        layoutManager = GridLayoutManager(root.context, 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return 2
            }
        }
        recyclerView.layoutManager = layoutManager
        adapter = TestAdapter(childFragmentManager)
        adapter.selectedPages = (savedInstanceState?.getSerializable(POSITIONS) as? HashMap<Int, Int>) ?: HashMap()
        recyclerView.adapter = adapter

        return root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(POSITIONS, HashMap(adapter.selectedPages))
    }

    val POSITIONS = "POSITIONS"
}

private class TestAdapter(
    val fragmentManager: FragmentManager
): RecyclerView.Adapter<ViewHolder>() {
    val items = listOf(
        TestItem.TextItem("text1"),
        TestItem.GalleryItem(numbers = listOf(1,2,4,5,6,7,8,9), 1),
        TestItem.GalleryItem(numbers = listOf(31,32,34,35,36,37,38,39), 2),
        TestItem.TextItem("text2"),
        TestItem.TextItem("text3"),
        TestItem.TextItem("text4"),
        TestItem.TextItem("text5"),
        TestItem.TextItem("text6"),
        TestItem.TextItem("text7"),
        TestItem.TextItem("text8"),
        TestItem.TextItem("text9"),
        TestItem.TextItem("text10"),
        TestItem.GalleryItem(numbers = listOf(10,20,40,50,60,70,80,90), 3),
        TestItem.TextItem("text11"),
        TestItem.TextItem("text12"),
        TestItem.TextItem("text13"),
        TestItem.TextItem("text14")
    )

    var fragments = mutableMapOf<Int, Fragment>()
    var selectedPages = hashMapOf<Int, Int>()//(1 to 2, 2 to 3, 3 to 4)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TestItem.TextItem.Type -> {
                TextViewHolder(
                    layoutInflater.inflate(R.layout.item_text, parent, false)
                )
            }
            TestItem.GalleryItem.Type -> {
                GalleryViewHolder(
                    layoutInflater.inflate(R.layout.item_gallery, parent, false),
                    fragmentManager
                )
            }
            else -> {
                throw Throwable("wrong viewType")
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is TextViewHolder -> {
                val data = items[position] as TestItem.TextItem
                holder.show(data)
            }
            is GalleryViewHolder -> {
                val data = items[position] as TestItem.GalleryItem
                holder.show(data)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].type
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)

        if (holder is GalleryViewHolder) {
            var fragment = fragments[holder.id]
            if (fragment == null) {
                fragment = fragmentManager.findFragmentByTag("gallery" + holder.id)

                if (fragment == null) {
                    fragment = GalleryViewFragment()

                    fragmentManager.beginTransaction()
                        .add(fragment, "gallery" + holder.id)
                        .setMaxLifecycle(fragment, Lifecycle.State.STARTED)
                        .commitNow();
                } else {
                    fragmentManager.beginTransaction().show(fragment).commitNow()
                }
            } else {
                fragmentManager.beginTransaction().show(fragment).commitNow()
            }
            val safeFragment = fragment as GalleryViewFragment
            fragments[holder.id] = safeFragment

            val fl = holder.itemView as FrameLayout
            fl.removeAllViews()
            fl.addView(safeFragment.view, 0, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT))

            holder.fragment = safeFragment
            holder.fragment?.viewPager?.show(holder.numbers, safeFragment.childFragmentManager)

            val pos = selectedPages[holder.id] ?: 0
            safeFragment.viewPager?.setCurrentItem(pos, false)

            holder.viewPager?.pageChangeListener = object : GalleryView.PageChangeListener {
                override fun onPageChanged(page: Int) {
                    selectedPages[holder.id] = page
                }
            }
        }
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)

        if (holder is GalleryViewHolder) {
            val fragment = fragments[holder.id]!!
            fragmentManager.beginTransaction().hide(fragment).commitNow();
            holder.viewPager?.pageChangeListener = null
            holder.fragment = null
            val fl = holder.itemView as FrameLayout
            fl.removeAllViews()
        }
    }
}

private class TextViewHolder(view: View): ViewHolder(view) {
    val textView = view.findViewById<TextView>(R.id.text)!!

    fun show(item: TestItem.TextItem) {
        textView.text = item.text
    }
}

private class GalleryViewHolder(view: View, val fragmentManager: FragmentManager): ViewHolder(view) {
    var fragment: GalleryViewFragment? = null

    var id: Int = 0
    var numbers: List<Int> = emptyList()

    val viewPager: GalleryView?
        get() {
            return fragment?.viewPager
        }

    fun show(item: TestItem.GalleryItem) {
        this.id = item.id
        this.numbers = item.numbers
    }
}

public class GalleryFragment: Fragment {
    constructor() : super(R.layout.gallery_fragment_view)
    constructor(contentLayoutId: Int) : super(contentLayoutId)
}

public class GalleryViewFragment: Fragment {
    constructor() : super(R.layout.item_gallery_view_pager)
    constructor(contentLayoutId: Int) : super(contentLayoutId)

    val viewPager: GalleryView?
        get() {
            return view?.findViewById(R.id.view_pager)
        }
}

public class GalleryView: ViewPager {
    var pageChangeListener: PageChangeListener? = null
    private var numbers: List<Int> = emptyList()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    init {
        addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                pageChangeListener?.onPageChanged(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }

    fun show(data: List<Int>, fragmentManager: FragmentManager) {
        if (adapter == null) {
            adapter = Adapter(fragmentManager).apply {
                offscreenPageLimit = 1
            }
        }

        numbers = data

        val galleryAdapter = adapter as Adapter
        galleryAdapter.setData(data)
    }

    interface PageChangeListener {
        fun onPageChanged(page: Int)
    }

    class Adapter: FragmentStatePagerAdapter {
        private var numbers: List<Int> = emptyList()

        constructor(fm: FragmentManager) : super(fm)
        constructor(fm: FragmentManager, behavior: Int) : super(fm, behavior)

        fun setData(data: List<Int>) {
            numbers = data
            notifyDataSetChanged()
        }

        override fun getCount(): Int {
            return numbers.size
        }

        override fun getItem(position: Int): Fragment {
            return NumberFragment().apply {
                show(numbers.get(position))
            }
        }
    }

    class NumberFragment: Fragment {
        val STATE_KEY = "number_state"
        var number: Int = -1
        var textView: TextView? = null

        constructor() : super(R.layout.gallery_fragment_view)
        constructor(contentLayoutId: Int) : super(contentLayoutId)

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            textView = view.findViewById(R.id.text)
        }

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            outState.putInt(STATE_KEY, number)
        }

        override fun onViewStateRestored(savedInstanceState: Bundle?) {
            super.onViewStateRestored(savedInstanceState)
            if (savedInstanceState != null) {
                number = savedInstanceState?.getInt(STATE_KEY)
            }

            textView?.text = number.toString()
        }

        fun show(data: Int) {
            number = data
            textView?.text = number.toString()
        }
    }
}

private open class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
}

sealed class TestItem(val type: Int) {
    data class TextItem(val text: String): TestItem(Type) {
        companion object {
            const val Type = 1
        }
    }
    data class GalleryItem(val numbers: List<Int>, val id: Int): TestItem(Type) {
        companion object {
            const val Type = 2
        }
    }
}
