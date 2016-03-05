$(document).ready(function () {

  var ViewModel = function () {
    this.users = ko.observableArray([]);
    this.response = ko.observable('');

    this.deleteUserData = (user) =>
      $.ajax({
        type: 'DELETE',
        url: '/api/admin/data/' + encodeURIComponent(user),
        success: (response) => this.users.remove(user),
        error: () => this.response('Failed.'),
      });

    this.getUsers = () =>
      $.ajax({
        type: 'GET',
        url: '/api/admin/users',
        success: (response) => this.users(response),
        error: () => this.response('Failed.'),
      });

    this.logout = () =>
      $.ajax({
        type: 'GET',
        url: '/api/logout',
        success: (response) => location.href = '/',
        error: () => this.response('Failed.'),
      });
  };

  var viewModel = new ViewModel();
  ko.applyBindings(viewModel);
  viewModel.getUsers();
});
