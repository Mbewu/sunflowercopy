package com.example.sunflower_copy.planted

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.sunflower_copy.R
import com.example.sunflower_copy.databinding.FragmentFullscreenImageBinding
import com.example.sunflower_copy.databinding.FragmentTitleBinding
import com.example.sunflower_copy.util.loadImage
import timber.log.Timber
import java.io.File

class FullscreenImageFragment : Fragment() {

    private lateinit var binding: FragmentFullscreenImageBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_fullscreen_image,container,false)

        // all we need to do, is get the argument that was passed to it and put it in the image view
        val imageFileName = FullscreenImageFragmentArgs.fromBundle(requireArguments()).imageFileName
        val path = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()
        val imageFile = File("$path/$imageFileName")

        Timber.i("file = $imageFile")
        loadImage(binding.imageView,imageFile)


        // Inflate the layout for this fragment
        return binding.root
    }
}