# Last Deployed Version
http://www.magic-inside.de/TreeView/

# Search for corresponding articles
We use the merchandise group as parameters to search for the articles of a group in the tree. The query url template is like this:

http://solr-artcache.was.metro.info:20100/solr-artcache/all/mcc/v1/solr-artcache_all_mcc_v3/select?q=dynLong_merchGroupMain:"668"+%0AAND+dynLong_merchGroup:"30"+%0AAND+dynLong_merchGroupSub:"25"+&fq=scope:+"erp_ihds_tree"+&fq=country:+"fr"+&fq=bundleNo:"1"+&rows=5&wt=csv

It searches for the first fife articles of 668/30/25

You'll find a XLS with the full audio part of the tree in the root of the project 'taxonomy_pack1_v1.0.xlsx'

# Tree View Lib
I use bootstrap-treeview to show the hierarchy

https://github.com/jonmiles/bootstrap-treeview
