package com.microsoft.azure.toolkit.ide.guidance.view.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.jcef.*;
import com.intellij.util.ui.JBUI;
import com.microsoft.azure.toolkit.ide.guidance.Phase;
import com.microsoft.azure.toolkit.ide.guidance.Step;
import lombok.SneakyThrows;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.handler.CefRequestHandlerAdapter;
import org.cef.network.CefRequest;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class DocPanel extends JPanel implements Disposable {
    private JBCefBrowser docBrowser;
    private JBCefJSQuery jsQuery;
    static final ObjectMapper mapper = new ObjectMapper();
    private String docData = "[\n" +
            "  '<div><div>Carousel in HTML</div> <p>Carousels require the use of an id (in this case id=\"myCarousel\" ) for carousel controls to function properly. The class=\"carousel\" specifies that this contains a carousel. The . slide class adds a CSS transition and animation effect, which makes the items slide when showing a new item.</p> </div>',\n" +
            "  '<div><q>But man is not made for defeat. A man can be destroyed but not defeated.</q> <p class=\"author\">- Ernest Hemingway</p></div>',\n" +
            "  '<div><q>But man is not made for defeat. A man can be destroyed but not defeated.</q> <p class=\"author\">- Ernest Hemingway</p></div>',\n" +
            "  '<div><q>I have not failed. I\\'ve just found 10,000 ways that won\\'t work.</q> <p class=\"author\">- Thomas A. Edison</p></div>',\n" +
            "]\n";

    public DocPanel() {
        super(new BorderLayout());
        this.init();
    }

    public void init() {
        final URL url = this.getClass().getClassLoader().getResource("slider.html");
        this.docBrowser = new JCEFHtmlPanel("C:\\Users\\wangmi\\workspace\\azure-tools-for-java\\PluginsAndFeatures\\azure-toolkit-for-intellij\\azure-intellij-plugin-guidance\\src\\main\\resources\\slider.html");
        this.add(this.docBrowser.getComponent(), BorderLayout.CENTER);
        final CefBrowser browser = docBrowser.getCefBrowser();
        final JBCefClient client = docBrowser.getJBCefClient();
        this.jsQuery = JBCefJSQuery.create((JBCefBrowserBase) this.docBrowser);
        this.jsQuery.addHandler((e) -> new JBCefJSQuery.Response("ok!"));
        client.addRequestHandler(openLinkWithLocalBrowser(), browser);
        client.addLoadHandler(updateHelpDocOnLoaded(), browser);
    }

    @Nonnull
    private CefLoadHandlerAdapter updateHelpDocOnLoaded() {
        return new CefLoadHandlerAdapter() {
            @Override
            public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
                refreshHelpDoc();
            }
        };
    }

    @SneakyThrows
    public void updateHelpDoc(@Nonnull Phase newPhase) {
        final List<String> docs = newPhase.getSteps().stream().map(Step::getTask)
                .map(t -> String.format("<div><div>%s</div><p>%s</p></div>", t.getName(), t.getDescription()))
                .collect(Collectors.toList());
        this.docData = mapper.writeValueAsString(docs);
        this.refreshHelpDoc();
    }

    private void refreshHelpDoc() {
        final CefBrowser browser = this.docBrowser.getCefBrowser();
        final String js = "" +
                "document.body.style.backgroundColor='" + ColorUtil.toHtmlColor(JBUI.CurrentTheme.ToolWindow.background()) + "';\n" +
                "var slidesData = " + this.docData + ";\n" +
                "currentSlide = 1;\n" +
                "initSlides(slidesData);\n" +
                "showSlide(currentSlide);\n";
        browser.executeJavaScript(js, browser.getURL(), 0);
    }

    @Nonnull
    private CefRequestHandlerAdapter openLinkWithLocalBrowser() {
        return new CefRequestHandlerAdapter() {
            @Override
            public boolean onBeforeBrowse(CefBrowser browser, CefFrame frame, CefRequest request, boolean user_gesture, boolean is_redirect) {
                if (request.getURL().startsWith("http")) {
                    BrowserUtil.browse(request.getURL());
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public void dispose() {
        Disposer.dispose(this.jsQuery);
        Disposer.dispose(this.docBrowser);
    }

    void $$$setupUI$$$() {
    }
}
