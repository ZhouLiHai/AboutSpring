package com.neptune8.controller;

import com.neptune8.model.Product;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ProductController {

	@RequestMapping("/save-product")
	public String uploadResource(HttpServletRequest servletRequest, @ModelAttribute Product product, Model model) {

		// 在servlet中定义了resolver之后就可以直接得到这个文件对象
		List<MultipartFile> files = product.getImages();

		// 文件存储过程，就是将得到的文件对象，存储到文件系统，这里也可以使用异步方式，将文件上传到其它的文件服务器中
		if (null != files && files.size() > 0) {
			for (MultipartFile multipartFile : files) {
				String fileName = multipartFile.getOriginalFilename();
				File imageFile = new File(servletRequest.getServletContext().getRealPath("/WEB-INF/image/"), fileName);

				try {
					multipartFile.transferTo(imageFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		model.addAttribute("product", product);
		return "viewProductDetail";
	}

	@RequestMapping(value = "/product")
	public String inputProduct(Model model) {
		model.addAttribute("product", new Product());
		return "productForm";
	}
}
