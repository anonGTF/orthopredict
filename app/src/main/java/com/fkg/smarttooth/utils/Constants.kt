package com.fkg.smarttooth.utils

import androidx.annotation.StringRes
import com.fkg.smarttooth.R

@StringRes
val TAB_TITLES = intArrayOf(
    R.string.tab_atas,
    R.string.tab_bawah,
)

const val HELP_BODY_MAIN = "<ul>\n" +
        "   <li>\n" +
        "    \tPada halaman utama terdapat list pasien yang telah ditambahkan oleh dokter/mahasiswa kedokteran. Klik pada nama pasien ntuk mengetahui detail hasil diagnosis.\n" +
        "    </li>\n" +
        "    <li>\n" +
        "    \tUntuk mencari pasien, klik pada search box \"Cari nama pasien\". Masukkan nama pasien yang ingin dicari. Tunggu beberapa saat, maka akan tampil pasien-pasien dengan nama sesuai yang Anda masukkan pada search box.\n" +
        "    </li>\n" +
        "    <li>\n" +
        "    \tKlik tombol \"LOGIN\" yang berada di pojok kanan atas. Perhatikan bahwa fitur LOGIN hanya untuk dokter/mahasiswa kedokteran.\n" +
        "    </li>\n" +
        "    <li>\n" +
        "    \tApabila Anda ingin menambahkan data pasien baru, klik tombol dengan icon \"+\" yang berada di pojok kiri bawah. Pastikan bahwa Anda sudah login terlebih dahulu karena tombol hanya muncul apabila pengguna sudah login.\n" +
        "    </li>\n" +
        "    </li>\n" +
        "    <li>\n" +
        "    \tApabila Anda ingin menuju profil pengguna, klik tombol dengan icon berbentuk orang yang berada di pojok kanan atas.\n" +
        "    </li>\n" +
        "</ul>"

const val HELP_BODY_DETAIL = "<ul>\n" +
        "\t<li>\n" +
        "    \tPada halaman detail Anda bisa melihat data gigi pasien dengan menekan bagian \"Data Gigi Pasien\". Anda akan diarahkan pada halaman baru \"Data Gigi Pasien\".\n" +
        "    </li>\n" +
        "\t<li>\n" +
        "    \tPada halaman detail juga terdapat data hasil diagnosis. Anda bisa melihat masing-masing hasil diagnosis pada list di bawah \"Data Gigi Pasien\". Klik judul diagnosis yang ingin Anda ketahui detailnya. Maka akan muncul keterangan hasil diagnosis. Anda juga bisa menekan icon berbentuk \"˅\" untuk membuka detail hasil diagnosis.\n" +
        "    </li>\n" +
        "    <li>\n" +
        "    \tTekan tombol dengan icon pensil (background hijau) untuk mengedit data diri pasien. Hanya pengguna yang sudah login yang bisa mengakses fitur ini!\n" +
        "    </li>\n" +
        "    <li>\n" +
        "    \tTekan tombol dengan icon tempat sampah (background merah) untuk menghapus data pasien terkait. Hanya pengguna yang sudah login yang bisa mengakses fitur ini!\n" +
        "    </li>\n" +
        "</ul>"

const val HELP_BODY_INPUT_DATA = "<ul>\n" +
        "\t<li>\n" +
        "    \tPada halaman ini terdapat detail data gigi pasien. Anda bisa melihat data gigi  rahang atas dengan tab \"Atas\" dan data gigi rahang bawah dengan tab \"Bawah\". Anda bisa menggeser ke kanan/kiri untuk berpindah tab atau dengan menekan judul tab.\n" +
        "    </li>\n" +
        "    <li>\n" +
        "    \tApabila Anda telah login dan ingin mengedit data gigi pasien, klik tombol Edit yang berada di bawah tampilan. Perhatikan bahwa semua isian menggunakan satuan milimeter (mm). Gigi/bagian yang Anda edit akan berubah menjadi berwarna biru ketika Anda mengetik di form isian terkait. Contohnya, ketika Anda mengetik di form Molar 1, gigi dengan nomor 26 akan berubah warna menjadi biru.\n" +
        "    </li>\n" +
        "    <li>\n" +
        "    \tKlik tombol \"Kembali\" apabila Anda batal mengedit data gigi pasien. Perhatikan bahwa perubahan yang Anda lakukan tidak akan tersimpan apabila menekan tombol kembali.\n" +
        "    </li>\n" +
        "    <li>\n" +
        "    \tKlik tombol \"Simpan\" untuk menyimpan perubahan yang Anda lakukan terhadap gigi pasien. Anda bisa langsung melihat perubahan diagnosis apabila Anda menekan icon back/kembali ke tampilan detail pasien.\n" +
        "    </li>\n" +
        "</ul>"