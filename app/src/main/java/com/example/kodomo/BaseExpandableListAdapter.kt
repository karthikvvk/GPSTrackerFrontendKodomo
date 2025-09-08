package com.example.kodomo


import android.content.Context
import android.view.*
import android.widget.*
import com.example.data.*
import com.example.kodomo.data.*

class HistoryExpandableAdapter(
    private val context: Context,
    private val data: Map<String, List<DataclassDb>>
) : BaseExpandableListAdapter() {
    private val hours = data.keys.sorted()

    override fun getGroupCount() = hours.size
    override fun getChildrenCount(groupPosition: Int) = data[hours[groupPosition]]?.size ?: 0
    override fun getGroup(groupPosition: Int) = hours[groupPosition]
    override fun getChild(groupPosition: Int, childPosition: Int) = data[hours[groupPosition]]?.get(childPosition)

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(android.R.layout.simple_expandable_list_item_1, parent, false)
        (view.findViewById<TextView>(android.R.id.text1)).text = hours[groupPosition]
        return view
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean,
                              convertView: View?, parent: ViewGroup?): View {
        val log = getChild(groupPosition, childPosition)
        val view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
        (view.findViewById<TextView>(android.R.id.text1)).text = "x=${log?.xCord}, y=${log?.yCord}, t=${log?.loggedTime}"
        return view
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int) = false
    override fun hasStableIds() = false
    override fun getGroupId(groupPosition: Int) = groupPosition.toLong()
    override fun getChildId(groupPosition: Int, childPosition: Int) = childPosition.toLong()
}
