//
// $Id$

package coreen.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Value;

import coreen.client.Args;
import coreen.model.CompUnitDetail;
import coreen.model.DefContent;
import coreen.model.DefId;
import coreen.model.DefInfo;
import coreen.model.Project;
import coreen.model.Span;
import coreen.ui.PopupGroup;
import coreen.ui.UIResources;
import coreen.ui.UIUtil;
import coreen.ui.WindowFX;
import coreen.util.DefMap;
import coreen.util.PanelCallback;

/**
 * Displays a single compilation unit.
 */
public class SourcePanel extends AbstractProjectPanel
{
    /** A source panel that displays an entire compilation unit. */
    public static class Full extends SourcePanel {
        public Full () {
            super(new DefMap(), UsePopup.SOURCE);
        }
    }

    public SourcePanel (DefMap defmap, UsePopup.Linker linker)
    {
        initWidget(_binder.createAndBindUi(this));
        _contents.addStyleName(_styles.codeMode());
        _contents.add(Widgets.newLabel("Loading..."));
        _defmap = defmap;
        _linker = linker;
        _local = new DefMap(_defmap);
    }

    public SourcePanel (long defId, DefMap defmap, UsePopup.Linker linker)
    {
        this(defmap, linker);
        loadDef(defId);
    }

    public SourcePanel (DefInfo def, DefMap defmap, UsePopup.Linker linker)
    {
        this(defmap, linker);
        init(def.sig, def.sigDefs, def.sigUses, -1L, false);
        removeStyleName(_styles.codeMode()); // no white-space: pre
    }

    public SourcePanel (String text, Span use, DefMap defmap, UsePopup.Linker linker)
    {
        this(defmap, linker);
        init(text, new Span[0], new Span[] { use }, -1L, true);
    }

    /**
     * Loads the source for the specified def into this panel.
     */
    public void loadDef (long defId)
    {
        _projsvc.getContent(defId, new PanelCallback<DefContent>(_contents) {
            public void onSuccess (DefContent content) {
                init(content);
            }
        });
    }

    /**
     * Initializes this source panel with the supplied def content.
     */
    public void init (DefContent content)
    {
        init(content.text, content.defs, content.uses, -1L, false);
    }

    /**
     * Adds the supplied icon to the beginning of the first line.
     */
    public void addFirstLineIcon (Widget icon)
    {
        ((FlowPanel)_contents.getWidget(0)).insert(icon, 0);
    }

    @Override // from AbstractProjectPanel
    public ProjectPage.Detail getId ()
    {
        return ProjectPage.Detail.SRC;
    }

    @Override // from AbstractProjectPanel
    public void setArgs (Project proj, Args args)
    {
        final long scrollToDefId = args.get(3, 0L);
        _projsvc.getCompUnit(args.get(2, 0L), new PanelCallback<CompUnitDetail>(_contents) {
            public void onSuccess (CompUnitDetail detail) {
                init(detail.text, detail.defs, detail.uses, scrollToDefId, false);
            }
        });
    }

    @Override // from Widget
    public void setVisible (boolean visible)
    {
        super.setVisible(visible);
        if (visible) {
            _local.addTo(_defmap);
        } else {
            _local.removeFrom(_defmap);
        }
    }

    @Override // from Widget
    protected void onLoad ()
    {
        super.onLoad();
        _local.addTo(_defmap);
    }

    @Override // from Widget
    protected void onUnload ()
    {
        super.onUnload();
        _local.removeFrom(_defmap);
    }

    protected void init (String text, Span[] defs, Span[] uses, long scrollToDefId,
                         boolean disablePrefixTrim)
    {
        _contents.clear();

        // TODO: make sure this doesn't freak out when source uses CRLF
        JsArrayString lines = splitString(text, "\n");
        String first = expandTabs(lines.get(0));
        int prefix = disablePrefixTrim ? 0 : first.indexOf(first.trim());
        if (prefix > 0) {
            // scan through another ten lines to ensure that the first line wasn't anomalous in
            // establishing our indentation prefix
            for (int ii = 0, ll = Math.min(lines.length(), 10); ii < ll; ii++) {
                String line = expandTabs(lines.get(ii)), tline = line.trim();
                if (tline.length() != 0 && // line is not blank
                    line.substring(0, Math.min(line.length(), prefix)).trim().length() > 0) {
                    prefix = line.indexOf(tline);
                }
            }
        }

        List<Elementer> elems = new ArrayList<Elementer>();
        for (final Span def : defs) {
            elems.add(new Elementer(def.getStart(), def.getStart()+def.getLength()) {
                public Widget createElement (final String text) {
                    final Label deflbl = Widgets.newInlineLabel(
                        text, DefUtil.getDefStyle(def.getKind()));
                    deflbl.addClickHandler(new ClickHandler() {
                        public void onClick (ClickEvent event) {
                            if (_menu == null) {
                                DefId did = new DefId();
                                did.id = def.getId();
                                did.name = text;
                                did.kind = def.getKind();
                                _menu = createDefPopup(did, deflbl);
                            }
                            Popups.show(_menu, Popups.Position.ABOVE, deflbl);
                        }
                        protected PopupPanel _menu;
                    });
                    deflbl.setTitle(""+def.getId());
                    _local.map(def.getId(), deflbl);
                    return deflbl;
                }
            });
        }
        for (final Span use : uses) {
            elems.add(new Elementer(use.getStart(), use.getStart()+use.getLength()) {
                public Widget createElement (String text) {
                    final Label span = Widgets.newInlineLabel(
                        text, DefUtil.getUseStyle(use.getKind()));
                    new UsePopup.Popper(use.getId(), span, _linker, _local, false).
                        setGroup(_pgroup);
                    UIUtil.makeActionable(span, new ClickHandler() {
                        public void onClick (ClickEvent event) {
                            if (_usesrc == null) {
                                _usesrc = new DefSourcePanel(use.getId(), _defmap, _linker);
                                _usesrc.addStyleName(_styles.nested());
                                addAfterLine((FlowPanel)span.getParent(), _usesrc);
                            } else {
                                _usesrc.setVisible(!_usesrc.isVisible());
                            }
                        }
                        protected DefSourcePanel _usesrc;
                    });
                    return span;
                }
            });
        }
        Collections.sort(elems);

        int offset = 0;
        FlowPanel curline = null;
        for (Elementer elem : elems) {
            if (elem.startPos < 0) continue; // filter undisplayable elems
            if (elem.startPos > offset) {
                if (elem.startPos >= text.length()) {
                    GWT.log("Invalid element? " + elem + " " + text.length() + " " + elem.startPos);
                    continue;
                }
                String seg = expandTabs(text.substring(offset, elem.startPos));
                // special handling for the first line since we can't rely on it following a
                // newline to tell us that it needs to be trimmed
                if (offset == 0 && prefix > 0) {
                    seg = seg.substring(prefix);
                }
                curline = appendText(curline, trimPrefix(seg, prefix));
            }
            if (elem.endPos > text.length()) {
                GWT.log("Invalid element " + elem.startPos + ":" + elem.endPos + " exceeds " +
                        text.length());
            } else {
                if (curline == null) {
                    _contents.add(curline = Widgets.newFlowPanel(_styles.line()));
                }
                curline.add(elem.createElement(text.substring(elem.startPos, elem.endPos)));
            }
            offset = elem.endPos;
        }
        if (offset < text.length()) {
            curline = appendText(curline, trimPrefix(expandTabs(text.substring(offset)), prefix));
        }

        final Widget scrollTo = _local.get(scrollToDefId);
        if (scrollTo != null) {
            GWT.log("Scrolling to " + scrollToDefId);
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                public void execute () {
                    WindowFX.scrollTo(scrollTo);
                }
            });
        }

        _local.addTo(_defmap);
        didInit();
    }

    protected FlowPanel appendText (FlowPanel curline, String text)
    {
        if (curline == null) {
            _contents.add(curline = Widgets.newFlowPanel(_styles.line()));
        }
        int eol = text.indexOf("\n");
        if (eol == -1) {
            curline.add(Widgets.newInlineLabel(text));
            return curline;
        } else {
            String line = text.substring(0, eol);
            // if we're appending a completely blank line, we need to insert a space, otherwise the
            // browser will collapse the line div completely
            if (curline.getWidgetCount() == 0 && line.length() == 0) {
                line = " ";
            }
            curline.add(Widgets.newInlineLabel(line));
            return appendText(null, text.substring(eol+1));
        }
    }

    protected void didInit ()
    {
    }

    protected void addAfterLine (FlowPanel line, Widget widget)
    {
        FlowPanel lines = (FlowPanel)line.getParent();
        int lidx = lines.getWidgetIndex(line);
        lines.insert(widget, lidx+1);
    }

    protected PopupPanel createDefPopup (final DefId def, final Widget deflbl)
    {
        final PopupPanel mpopup = createPopupPanel();
        MenuBar menu = new MenuBar(true);
        menu.addItem(new MenuItem("Show uses...", new Command() {
            public void execute () {
                mpopup.hide();
                PopupPanel spopup = createPopupPanel();
                PopupGroup.Positioner repos = createPositioner(spopup, deflbl);
                spopup.setWidget(new DefUsesPanel(def, _defmap, repos));
                repos.sizeDidChange();
            }
        }));
        menu.addItem(new MenuItem("Show supers...", new Command() {
            public void execute () {
                mpopup.hide();
                PopupPanel spopup = createPopupPanel();
                PopupGroup.Positioner repos = createPositioner(spopup, deflbl);
                spopup.setWidget(new SuperTypesPanel(def.getId(), _defmap, repos));
                repos.sizeDidChange();
            }
        }));
        menu.addItem(new MenuItem("Show subs...", new Command() {
            public void execute () {
                mpopup.hide();
                PopupPanel spopup = createPopupPanel();
                PopupGroup.Positioner repos = createPositioner(spopup, deflbl);
                spopup.setWidget(new SubTypesPanel(def.getId(), _defmap, repos));
                repos.sizeDidChange();
            }
        }));
        mpopup.setWidget(menu);
        return mpopup;
    }

    protected PopupPanel createPopupPanel ()
    {
        PopupPanel panel = new PopupPanel(true);
        panel.addStyleName(_uirsrc.styles().popup());
        return panel;
    }

    protected PopupGroup.Positioner createPositioner (final PopupPanel popup, final Widget target)
    {
        return new PopupGroup.Positioner() {
            public void sizeDidChange () {
                Popups.show(popup, Popups.Position.ABOVE, target);
            }
        };
    }

    protected abstract class Elementer implements Comparable<Elementer> {
        public final int startPos;
        public final int endPos;

        public abstract Widget createElement (String text);

        public int compareTo (Elementer other) {
            return startPos - other.startPos;
        }

        protected Elementer (int startPos, int endPos) {
            this.startPos = startPos;
            this.endPos = endPos;
        }
    }

    // TODO: allow per-project settings
    protected static String expandTabs (String text)
    {
        return text.replace("\t", "        ");
    }

    protected native JsArrayString splitString (String text, String delim) /*-{
        return text.split(delim);
    }-*/;

    protected native String trimPrefix (String text, int prefix) /*-{
        if (prefix == 0) {
            return text;
        }
        var lines = text.split("\n");
        var ii, ll = lines.length;
        for (ii = 1; ii < ll; ii++) {
           var line = lines[ii];
           if (line.length > prefix) { // TODO: avoid chopping non-whitespace
             lines[ii] = line.substring(prefix);
           }
         }
         return lines.join("\n");
    }-*/;

    protected DefMap _defmap, _local;
    protected UsePopup.Linker _linker;
    protected PopupGroup _pgroup = new PopupGroup();

    protected interface Styles extends CssResource {
        String line ();
        String codeMode ();
        String nested ();
    }
    protected @UiField Styles _styles;
    protected @UiField FlowPanel _contents;

    protected interface Binder extends UiBinder<Widget, SourcePanel> {}
    protected static final Binder _binder = GWT.create(Binder.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
    protected static final UIResources _uirsrc = GWT.create(UIResources.class);
}
