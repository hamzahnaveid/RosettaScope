package com.example.rosettascope.fragments

import  android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.example.rosettascope.R


private val CAMERA_PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)
private val RECORD_AUDIO_PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.RECORD_AUDIO)

class PermissionsFragment : Fragment() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted: Boolean ->
            if (isGranted) {
                navigateToCamera()
            }
            else {
                Toast.makeText(
                    context, "Permission request denied",
                    Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
                    &&
                ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                navigateToCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun navigateToCamera() {
        lifecycleScope.launchWhenStarted {
            Navigation.findNavController(
                requireActivity(),
                R.id.fragment_container
            ).navigate(
                PermissionsFragmentDirections.actionPermissionsToCamera()
            )
        }
    }

    companion object {
        fun hasPermissions(context: Context) =
            CAMERA_PERMISSIONS_REQUIRED.all {
                ContextCompat.checkSelfPermission(
                    context,
                    it
                ) == PackageManager. PERMISSION_GRANTED
            } && RECORD_AUDIO_PERMISSIONS_REQUIRED.all {
                ContextCompat.checkSelfPermission(
                    context,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }
    }
}