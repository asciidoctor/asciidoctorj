package org.asciidoctor.extension;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.Column;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;
import org.asciidoctor.util.TestHttpServer;

public class GithubContributorsBlockMacro extends BlockMacroProcessor {

    private static final String IMAGE = "image";
    private static final String WIDTHS = "widths";

    // NOTE the fields of a contributor (the JSON was parsed with JsonSlurper in Groovy)
    private static final Pattern CONTRIBUTOR_RX = Pattern.compile(
            "\"login\":\\s*\"([^\"]*)\".*?\"avatar_url\":\\s*\"([^\"]*)\".*?\"contributions\":\\s*(\\d+)", Pattern.DOTALL);

    public GithubContributorsBlockMacro(String macroName) {
        super(macroName);
    }

    @Override
    public StructuralNode process(StructuralNode parent, String target, Map<String, Object> attributes) {
        String content;
        try {
            URL url = new URL("http://api.github.com/repos/" + target + "/contributors");
            URLConnection connection = url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", TestHttpServer.getInstance().getLocalPort())));
            try (InputStream in = connection.getInputStream()) {
                content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        List<Map<String, String>> contributors = new ArrayList<>();
        Matcher matcher = CONTRIBUTOR_RX.matcher(content);
        while (matcher.find()) {
            contributors.add(Map.of("login", matcher.group(1), "avatar_url", matcher.group(2), "contributions", matcher.group(3)));
        }

        // Create the table
        Table table = createTable(parent);
        table.setGrid("rows");
        table.setTitle("Github contributors of " + target);

        List<Integer> widths = List.of(1, 2, 2);
        if (attributes.containsKey(WIDTHS)) {
            widths = Arrays.stream(((String) attributes.get(WIDTHS)).split(",")).map(Integer::parseInt).collect(Collectors.toList());
        }
        // Create the columns 'Login' and 'Contributions'
        Column avatarColumn = createTableColumn(table, 0);
        avatarColumn.setWidth(widths.get(0));
        Column loginColumn = createTableColumn(table, 1);
        loginColumn.setWidth(widths.get(1));
        Column contributionsColumn = createTableColumn(table, 2);
        contributionsColumn.setWidth(widths.get(2));
        contributionsColumn.setHorizontalAlignment(Table.HorizontalAlignment.CENTER);
        table.getColumns().add(avatarColumn);
        table.getColumns().add(loginColumn);
        table.getColumns().add(contributionsColumn);
        table.assignColumnWidths();
        // Create the single header row with the column titles
        Row headerRow = createTableRow(table);
        headerRow.getCells().add(createTableCell(avatarColumn, "Avatar"));
        headerRow.getCells().add(createTableCell(loginColumn, "Login"));
        headerRow.getCells().add(createTableCell(loginColumn, "Contributions"));
        table.getHeader().add(headerRow);

        // for each contributor create a cell with the login and the number of contributions
        for (Map<String, String> contributor : contributors) {
            Row row = createTableRow(table);

            Document document = createDocument(parent.getDocument());
            Map<String, Object> imageAttributes = new HashMap<>();
            imageAttributes.put("type", IMAGE);
            imageAttributes.put("target", contributor.get("avatar_url"));
            imageAttributes.put("alt", "Avatar of " + contributor.get("login"));
            document.getBlocks().add(createBlock(document, IMAGE, (String) null, imageAttributes));
            Cell avatarCell = createTableCell(loginColumn, document);
            row.getCells().add(avatarCell);
            row.getCells().add(createTableCell(loginColumn, contributor.get("login")));
            row.getCells().add(createTableCell(contributionsColumn, contributor.get("contributions")));
            table.getBody().add(row);
        }
        return table;
    }
}
