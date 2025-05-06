package com.sougata.supplysync.pdf.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.sougata.supplysync.R
import com.sougata.supplysync.databinding.FragmentPdfViewerBinding
import com.sougata.supplysync.util.KeysAndMessages

class PdfViewerFragment : Fragment() {

    private var _binding: FragmentPdfViewerBinding? = null
    private val binding get() = _binding!!

    private var pdfByteArray: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.pdfByteArray = requireArguments().getByteArray(KeysAndMessages.BYTE_ARRAY_KEY)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        this._binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_pdf_viewer, container, false)

        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.binding.pdfView.fromBytes(this.pdfByteArray)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .onError {
                Log.d("PdfError", it.message.toString())
            }
            .load()

    }

}