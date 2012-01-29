/*!
 * swipeGallery 0.5.0
 * Examples and documentation at: 
 * http://www.app-agentur-bw.de/showcase/swipegallery
 * 2011 AREA-NET GmbH (www.app-agentur-bw.de | www.area-net.de)
 * Version: 0.5.0 (17-MARCH-2011)
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Requires:
 * jQuery v1.3.2 or later
 * 
 */

(function($){
  $.fn.swipeGallery = function(options) {
            
    var settings = {
      'classname'  : 'appGallery',
      'autoHeight' : false,
      'height'     : '600px',
      'width'      : '800px',
      'background' : '#000000',
      'tolerance'  : 0.25,
      'delay'      : 300
    }
    
    var mousedown       = false;
    var mouseX          = 0;
    var imagesLength    = 0;
    var imagesCurrent   = 0;
    var xdiff           = 0;
    var containerHeight = 0;
    var containerWidth  = 0;
    
    
    function doResizeImage(listElement){
        $(listElement).css('height', containerHeight);
        $(listElement).css('width', containerWidth);
        var img = $(listElement).find('img');
        
        if (img.width() / containerWidth > img.height() / containerHeight){
            img.width(containerWidth);
            
            var top = (containerHeight - img.height())/2; 
            img.css('marginTop', top + 'px'); 
        }else{
            img.height(containerHeight); 
            var left = parseInt((containerWidth - img.width())/2);
            img.css('marginLeft', left + 'px');
        }
                      
    }
    
    function init(obj, parent, imageHandler){
        if(settings.autoHeight){
           containerHeight = $(window).height();
           containerWidth  = $(window).width();
        }else{
           containerHeight = parseInt(settings.height);  
           containerWidth  = parseInt(settings.width);  
        }
        
        obj.find('li').each(function(){
            doResizeImage(this);
            imagesLength++;
        })
            
        parent.css('height', containerHeight);
        parent.css('width',  containerWidth);
        
        imageHandler.css('width', containerWidth);
        imageHandler.css('height', containerHeight);
        imageHandler.css('left', parent.position().left);
        imageHandler.css('top', parent.position().top);
        
        obj.css('width', imagesLength * containerWidth);
    }
        
    return this.each(function(){        
      
      var _this = $(this);
      if(options) { 
        $.extend(settings, options);
      }
      
      if(settings.autoHeight){
        containerHeight = $(window).height();
        containerWidth  = $(window).width();
      }else{
        containerHeight = parseInt(settings.height);  
        containerWidth  = parseInt(settings.width);  
      }

      _this.wrap('<div class="' + settings.classname + '"/>');
      
      var parent = _this.parent();
      parent.css('backgroundColor', settings.background); 
     
      parent.prepend('<div class="imageHandler"/>');
 
      var imageHandler = _this.parent().find('.imageHandler');
       
      init(_this, parent, imageHandler);
      
      $(window).resize(function(){
        init(_this, parent, imageHandler);   
      })
      
      imageHandler.mousedown(function(event){
        if(!this.mousedown){
            this.mousedown = true;
            this.mouseX = event.pageX;     
        }
        
        return false;
      });
      
      imageHandler.mousemove(function(event){
        if(this.mousedown){
            xdiff = event.pageX - this.mouseX;
            _this.css('left', -imagesCurrent * containerWidth + xdiff);
        }
        
        return false; 
      }); 
       
      imageHandler.mouseup(function(event){
        this.mousedown = false; 
        if(!xdiff) return false;
        
        var fullWidth = parseInt(settings.width);
        var halfWidth = fullWidth/2;
        
        if(-xdiff > halfWidth - fullWidth * settings.tolerance){
            imagesCurrent++;
            imagesCurrent = imagesCurrent >= imagesLength ? imagesLength-1 : imagesCurrent; 
            _this.animate({left: -imagesCurrent * containerWidth}, settings.delay);
        }else if(xdiff > halfWidth - fullWidth * settings.tolerance){
            imagesCurrent--;
            imagesCurrent = imagesCurrent < 0 ? 0 : imagesCurrent;
            _this.animate({left: -imagesCurrent * containerWidth}, settings.delay);  
        }else{
            _this.animate({left: -imagesCurrent * containerWidth}, settings.delay);
        }
        
        xdiff = 0;
        
        return false; 
      });
      
      imageHandler.mouseleave(function(event){
         imageHandler.mouseup();
      })
      
    });
    
  };
})(jQuery);