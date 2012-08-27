package edu.hiro.util;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractView;

import edu.hiro.util.WebHelper.ContentType;

// PNG is hard-coded for now
public class ImageView extends AbstractView implements View
{
	protected String contentType=ContentType.PNG;
	protected byte[] byteArray;
	protected BufferedImage bufferedImage;
	
	public ImageView(byte[] image)
	{
		if (image==null)
			createBlankImage();
		else this.byteArray=image;
	}
	
	public ImageView(BufferedImage image)
	{
		if (image==null)
			createBlankImage();
		this.bufferedImage=image;
	}
	
	private void createBlankImage()
	{
		System.err.println("image is null");
		bufferedImage=new BufferedImage(1,1,java.awt.image.BufferedImage.TYPE_INT_RGB);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		response.setContentType(contentType);
		ServletOutputStream out=response.getOutputStream();
		BufferedOutputStream bout=new BufferedOutputStream(out);
		if (this.byteArray!=null)
			ImageHelper.writeImage(this.byteArray, bout);
		else if (this.bufferedImage!=null)
			ImageHelper.writeImage(this.bufferedImage, ImageHelper.Format.png, bout);
		else throw new CException("no image data");
	}
}
