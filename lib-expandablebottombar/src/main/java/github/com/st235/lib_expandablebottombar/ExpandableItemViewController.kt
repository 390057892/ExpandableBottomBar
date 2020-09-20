package github.com.st235.lib_expandablebottombar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.Px
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat.setAccessibilityDelegate
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import github.com.st235.lib_expandablebottombar.components.ExpandableBottomBarMenuItemView
import github.com.st235.lib_expandablebottombar.utils.DrawableHelper
import github.com.st235.lib_expandablebottombar.utils.StyleController
import github.com.st235.lib_expandablebottombar.utils.createChain

internal class ExpandableItemViewController(
    internal val menuItem: ExpandableBottomBarMenuItem,
    private val itemView: ExpandableBottomBarMenuItemView
) {

    fun setAccessibleWith(prev: ExpandableItemViewController?,
                          next: ExpandableItemViewController?) {
        setAccessibilityDelegate(itemView, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View?, info: AccessibilityNodeInfoCompat?) {
                info?.setTraversalAfter(prev?.itemView)
                info?.setTraversalBefore(next?.itemView)
                super.onInitializeAccessibilityNodeInfo(host, info)
            }
        })
    }

    fun notification(): ExpandableBottomBarNotification {
        return ExpandableBottomBarNotification(itemView)
    }

    fun unselect() {
        itemView.deselect()
    }

    fun select() {
        itemView.select()
    }

    fun attachTo(parent: ConstraintLayout,
                 previousIconId: Int,
                 nextIconId: Int,
                 menuItemHorizontalMargin: Int,
                 menuItemVerticalMargin: Int) {
        val lp = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        lp.setMargins(menuItemHorizontalMargin, menuItemVerticalMargin,
            menuItemHorizontalMargin, menuItemVerticalMargin)

        parent.addView(itemView, lp)

        val cl = ConstraintSet()
        cl.clone(parent)

        cl.connect(itemView.id, ConstraintSet.TOP, parent.id, ConstraintSet.TOP)
        cl.connect(itemView.id, ConstraintSet.BOTTOM, parent.id, ConstraintSet.BOTTOM)

        if (previousIconId == itemView.id) {
            cl.connect(itemView.id, ConstraintSet.START, parent.id, ConstraintSet.START)
        } else {
            cl.connect(itemView.id, ConstraintSet.START, previousIconId, ConstraintSet.END)
            cl.createChain(previousIconId, itemView.id, ConstraintSet.CHAIN_PACKED)
        }

        if (nextIconId == itemView.id) {
            cl.connect(itemView.id, ConstraintSet.END, parent.id, ConstraintSet.END)
        } else {
            cl.connect(itemView.id, ConstraintSet.END, nextIconId, ConstraintSet.START)
            cl.createChain(itemView.id, nextIconId, ConstraintSet.CHAIN_PACKED)
        }

        cl.applyTo(parent)
    }

    //TODO(st235): separate this builder to view factory
    class Builder(private val menuItem: ExpandableBottomBarMenuItem) {

        @Px
        private var itemVerticalPadding: Int = 0
        @Px
        private var itemHorizontalPadding: Int = 0
        @Px
        @SuppressLint("SupportAnnotationUsage")
        private var backgroundCornerRadius: Float = 0.0f
        @FloatRange(from = 0.0, to = 1.0)
        private var backgroundOpacity: Float = 1.0f
        @ColorInt
        private var itemInactiveColor: Int = Color.BLACK
        @ColorInt
        private var notificationBadgeColor: Int = Color.RED
        @ColorInt
        private var notificationBadgeTextColor: Int = Color.WHITE

        private lateinit var styleController: StyleController
        private lateinit var onItemClickListener: (View) -> Unit

        fun itemMargins(
            @Px itemHorizontalPadding: Int,
            @Px itemVerticalPadding: Int
        ): Builder {
            this.itemVerticalPadding = itemVerticalPadding
            this.itemHorizontalPadding = itemHorizontalPadding
            return this
        }

        fun itemBackground(backgroundCornerRadius: Float,
                           @FloatRange(from = 0.0, to = 1.0) backgroundOpacity: Float): Builder {
            this.backgroundCornerRadius = backgroundCornerRadius
            this.backgroundOpacity = backgroundOpacity
            return this
        }

        fun itemInactiveColor(@ColorInt itemInactiveColor: Int): Builder {
            this.itemInactiveColor = itemInactiveColor
            return this
        }

        fun onItemClickListener(onItemClickListener: (View) -> Unit): Builder {
            this.onItemClickListener = onItemClickListener
            return this
        }

        fun styleController(styleController: StyleController): Builder {
            this.styleController = styleController
            return this
        }

        fun notificationBadgeColor(@ColorInt notificationBadgeColor: Int): Builder {
            this.notificationBadgeColor = notificationBadgeColor
            return this
        }

        fun notificationBadgeTextColor(@ColorInt notificationBadgeTextColor: Int): Builder {
            this.notificationBadgeTextColor = notificationBadgeTextColor
            return this
        }

        private fun createHighlightedMenuShape(): Drawable {
            return styleController.createStateBackground(
                menuItem.activeColor,
                backgroundCornerRadius,
                backgroundOpacity
            )
        }

        private fun createMenuItemView(context: Context): ExpandableBottomBarMenuItemView {
            return ExpandableBottomBarMenuItemView(context = context)
        }

        fun build(context: Context): ExpandableItemViewController {
            val itemView = createMenuItemView(context)
            val backgroundColorStateList = DrawableHelper.createSelectedUnselectedStateList(
                menuItem.activeColor,
                itemInactiveColor
            )

            with(itemView) {
                id = menuItem.itemId
                contentDescription = context.resources.getString(R.string.accessibility_item_description, menuItem.text)
                setPadding(itemHorizontalPadding, itemVerticalPadding, itemHorizontalPadding, itemVerticalPadding)

                setIcon(menuItem.iconId, backgroundColorStateList)
                setText(menuItem.text, backgroundColorStateList)
                setNotificationBadgeBackground(menuItem.badgeBackgroundColor ?: notificationBadgeColor)
                setNotificationBadgeTextColor(menuItem.badgeTextColor ?: notificationBadgeTextColor)

                background = createHighlightedMenuShape()
                setOnClickListener(onItemClickListener)
            }

            return ExpandableItemViewController(
                menuItem,
                itemView
            )
        }
    }
}
