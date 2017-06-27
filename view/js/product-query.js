$(function() {

    var $searchableTree = $('#treeview-searchable').treeview({
        color: "#428bca",
        expandIcon: 'glyphicon glyphicon-chevron-right',
        collapseIcon: 'glyphicon glyphicon-chevron-down',
        data: articleData,
        enableLinks: true,
        showTags: true
    });

    var search = function(e) {
        var pattern = $('#input-search').val();
        var options = {
            ignoreCase: $('#chk-ignore-case').is(':checked'),
            exactMatch: $('#chk-exact-match').is(':checked'),
            revealResults: $('#chk-reveal-results').is(':checked')
        };
        var results = $searchableTree.treeview('search', [pattern, options]);

        var output = '<ul class="list-group">';
        output += '<li class="list-group-item list-group-item-info">' + results.length + ' matches found</li>';
        $.each(results, function(index, result) {
            output += '<li class="list-group-item">' + result.text + '</li>';
        });
        output += '</ul>'
        $('#search-output').html(output);
    }

    $('#btn-search').on('click', search);
    $('#input-search').on('keyup', search);

    $('#btn-clear-search').on('click', function(e) {
        $searchableTree.treeview('clearSearch');
        $('#input-search').val('');
        $('#search-output').html('');
    });

    $('#btn-collaps-tree').on('click', function(e) {
        $searchableTree.treeview('collapseAll', { silent: true });
    });

});
