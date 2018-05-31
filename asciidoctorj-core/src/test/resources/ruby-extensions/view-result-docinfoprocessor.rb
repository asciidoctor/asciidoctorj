require 'asciidoctor/extensions' unless RUBY_ENGINE == 'opal'

# An extension that automatically hides blocks marked with the style
# "result" and adds a link to the previous element that has the style
# "title" that allows displaying the "result".
#
# Usage
#
#   = View Result Sample
#
#   .This will have a link next to it
#   ----
#   * always displayed
#   * always displayed 2
#   ----
#
#   [.result]
#   ====
#   * hidden till clicked
#   * hidden till clicked 2
#   ====
#
#
class ViewResultDocinfoProcessor < Asciidoctor::Extensions::DocinfoProcessor
  use_dsl
  at_location :head

  def process doc
    styles = %(<style>
.listingblock a.view-result {
  float: right;
  font-weight: normal;
  text-decoration: none;
  font-size: 0.9em;
  line-height: 1.4;
  margin-top: 0.15em;
}
</style>)

  scripts = %(<script>
function toggle_result_block(e) {
  var resultNode = e.target.parentNode.parentNode.nextElementSibling;
  resultNode.style.display = getComputedStyle(resultNode).display == 'none' ? '' : 'none';
  return false;
}
document.addEventListener('DOMContentLoaded', function() {
  [].forEach.call(document.querySelectorAll('.result'), function(resultNode) {
    resultNode.style.display = 'none';
    var viewLink = document.createElement('a');
    viewLink.className = 'view-result';
    viewLink.href = '#';
    viewLink.appendChild(document.createTextNode('[ view result ]'));
    resultNode.previousElementSibling.querySelector('.title').appendChild(viewLink);
    viewLink.addEventListener('click', toggle_result_block);
  });
});
</script>);
puts "I'm called"
    [styles, scripts].join "\n"
  end
end