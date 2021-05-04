package com.kc.michaelsshoppingcart.recyclerAdapters


import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.kc.michaelsshoppingcart.R
import com.kc.michaelsshoppingcart.classes.Product
import com.squareup.picasso.Picasso


class ProductListAdapter internal constructor(context: Context?, val onItemClick: (Product, Int) -> Unit) :
    RecyclerView.Adapter<ProductListAdapter.GenreViewHolder>() {
    private val mInflater: LayoutInflater
    private var mProducts // Cached copy of genres
            : List<Product>? = null
    private var mContext:Context? = context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GenreViewHolder {
        val itemView: View
           itemView = mInflater.inflate(R.layout.product_recyclerview_item, parent, false)

        return GenreViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: GenreViewHolder,
        position: Int
    ) {
        if (mProducts != null) {
            val current = mProducts!![position]
             holder.descriptionView.text = current.description
             holder.name.text = current.name
             holder.numberInStock.text = mContext!!.getString(R.string.number_in_stock,current.numberInStock)
             holder.buttonAddToCart.visibility=if(current.numberInStock>0 && current.addedToCart == 0 )View.VISIBLE else View.GONE
             holder.buttonRemoveFromCart.visibility=if(current.addedToCart>0)View.VISIBLE else View.GONE
             Picasso.get().load(current.picture)
                .error(R.drawable.iphone)
                .into(holder.icon)

        }
    }

     fun setProducts(products: List<Product>?) {
        mProducts = products
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return if (mProducts != null) mProducts!!.size else 0
    }

    inner class GenreViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val name: TextView
        val icon :ImageView
        val descriptionView:TextView
        val numberInStock:TextView
        val buttonAddToCart :Button
        val buttonRemoveFromCart :Button

        init {
            name = itemView.findViewById(R.id.textview_name)
            icon = itemView.findViewById(R.id.icon)
            descriptionView = itemView.findViewById(R.id.textview_description)
            numberInStock = itemView.findViewById(R.id.textview_number_in_stock)
            buttonAddToCart = itemView.findViewById(R.id.button_add_to_cart)
            buttonRemoveFromCart = itemView.findViewById(R.id.button_remove_from_cart)

            buttonRemoveFromCart.setOnClickListener {
                onItemClick(mProducts!!.get(adapterPosition),adapterPosition)
            }


            buttonAddToCart.setOnClickListener {
                onItemClick(mProducts!!.get(adapterPosition),adapterPosition)
            }
        }
    }

    init {
        mInflater = LayoutInflater.from(context)
    }
}