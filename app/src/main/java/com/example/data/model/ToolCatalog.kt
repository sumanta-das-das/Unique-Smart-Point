package com.example.data.model

object ToolCatalog {
    val categories = listOf(
        ServiceCategory("pdf", "PDF Tools", "Merge, split, compress, and convert PDF documents", 25, 0xFF2563EB),
        ServiceCategory("image", "Image Tools", "Resize, compress, convert, crop, and edit images", 25, 0xFF7C3AED),
        ServiceCategory("photo", "Photo Services", "Passport photos, ID cards, photo retouching and sheets", 24, 0xFF0891B2),
        ServiceCategory("ai", "AI Studio", "AI enhancer, background removal, text generator & OCR", 20, 0xFF9333EA),
        ServiceCategory("frame", "Photo Frame Studio", "Creative festival, birthday, and wedding photo frames", 15, 0xFFDB2777),
        ServiceCategory("doc", "Document Tools", "Resume maker, application letter, and word counter", 18, 0xFF059669),
        ServiceCategory("qr", "QR & Utilities", "QR generator, barcodes, age and unit calculators", 14, 0xFFD97706),
        ServiceCategory("booking", "Service Booking", "Book manual digital services and custom printing", 10, 0xFF4F46E5)
    )

    val tools = listOf(
        // Popular Tools
        ToolDefinition("passport_photo", "Passport Size Photo", "Create 3.5x4.5cm photos & 4x6 print sheets", "photo", isPopular = true, iconName = "badge"),
        ToolDefinition("photo_frame", "Photo Frame Maker", "Design custom festive and birthday photo frames", "frame", isPopular = true, iconName = "image"),
        ToolDefinition("img_to_pdf", "Image to PDF", "Convert multiple JPG/PNG images into a single PDF", "pdf", isPopular = true, iconName = "picture_as_pdf"),
        ToolDefinition("merge_pdf", "Merge PDF", "Combine multiple documents into one PDF file", "pdf", isPopular = true, iconName = "merge"),
        ToolDefinition("compress_img", "Compress Image", "Reduce image file size with custom quality slider", "image", isPopular = true, iconName = "compress"),
        ToolDefinition("resize_pixels", "Resize by Pixels", "Exact width & height pixel dimensions with aspect ratio", "image", isPopular = true, iconName = "aspect_ratio"),
        ToolDefinition("ai_bg_remove", "AI Background Remover", "Remove photo backgrounds to clean white or transparent", "ai", isPopular = true, isAi = true, iconName = "auto_fix_high"),
        ToolDefinition("ai_enhance", "AI Photo Enhancer", "Restore brightness, sharpness, and vibrant colors", "ai", isPopular = true, isAi = true, iconName = "auto_awesome"),
        ToolDefinition("signature_resizer", "Signature Resizer", "Resize signature to exact dimensions and KB limits", "photo", isPopular = true, iconName = "draw"),
        ToolDefinition("id_card_photo", "ID Card Photo Maker", "Standard layout for school, college & employee IDs", "photo", isPopular = true, iconName = "contact_page"),
        ToolDefinition("qr_code", "QR Code Generator", "Create QR codes for URLs, Wi-Fi, Contacts & UPI", "qr", isPopular = true, iconName = "qr_code"),
        ToolDefinition("doc_resume", "Resume & Biodata Maker", "Generate clean CVs and biodata for job applications", "doc", isPopular = true, iconName = "description"),
        ToolDefinition("doc_letter", "Application Letter Writer", "Formal letters in English and Bengali", "doc", isPopular = true, isAi = true, iconName = "mail"),
        ToolDefinition("service_booking", "Digital Service Booking", "Order manual photo editing and document verification", "booking", isPopular = true, iconName = "shopping_cart"),

        // Additional PDF Tools
        ToolDefinition("split_pdf", "Split PDF", "Extract specific pages from PDF documents", "pdf"),
        ToolDefinition("compress_pdf", "Compress PDF", "Optimize PDF size for online government submissions", "pdf"),
        ToolDefinition("rotate_pdf", "Rotate PDF", "Rotate PDF document pages by 90, 180, or 270 degrees", "pdf"),
        ToolDefinition("watermark_pdf", "Add Watermark to PDF", "Stamp custom confidentiality or ownership text", "pdf"),

        // Additional Image Tools
        ToolDefinition("convert_format", "Convert Image Format", "Switch between JPG, PNG, and WebP instantly", "image"),
        ToolDefinition("resize_percent", "Resize by Percentage", "Scale images to 25%, 50%, 75%, 150%, or 200%", "image"),
        ToolDefinition("crop_image", "Image Cropper", "Crop to 1:1, 4:3, 16:9, or freehand ratios", "image"),
        ToolDefinition("palette_extract", "Color Palette Extractor", "Extract hex colors from any photo", "image"),

        // Additional AI Tools
        ToolDefinition("ai_ocr", "OCR Text Extraction", "Extract editable text from scanned photos and forms", "ai", isAi = true),
        ToolDefinition("ai_bengali", "Bengali Text Generator", "Create festive wishes and business captions", "ai", isAi = true),
        ToolDefinition("ai_art", "Photo to Art & Sketch", "Convert portraits into pencil sketch and paintings", "ai", isAi = true),

        // Additional Utility Tools
        ToolDefinition("barcode", "Barcode Generator", "Generate Code-128 barcodes for retail and products", "qr"),
        ToolDefinition("age_calc", "Age Calculator", "Calculate exact age in years, months, and days", "qr"),
        ToolDefinition("word_counter", "Word & Character Counter", "Count lines, words, and characters with case conversion", "doc")
    )
}
